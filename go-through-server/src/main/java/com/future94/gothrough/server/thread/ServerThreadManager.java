package com.future94.gothrough.server.thread;

import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.thread.AbstractNIORunnable;
import com.future94.gothrough.protocol.thread.GoThroughNioContainer;
import com.future94.gothrough.protocol.thread.GoThroughNIORunnable;
import com.future94.gothrough.protocol.thread.GoThroughThreadFactory;
import com.future94.gothrough.server.config.ServerConfig;
import com.future94.gothrough.server.service.ServerService;
import com.future94.gothrough.server.service.impl.InteractiveServerService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author weilai
 */
@Slf4j
public class ServerThreadManager extends AbstractNIORunnable {

    private final ServerConfig config;

    private final ServerSocket listenServerSocket;

    private final ServerService<?, ?> serverService;

    private AtomicBoolean isAlive = new AtomicBoolean(false);

    private AtomicBoolean isCancel = new AtomicBoolean(false);

    private final ExecutorService executorService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), GoThroughThreadFactory.create("socket-accept-loop"), new ThreadPoolExecutor.CallerRunsPolicy());

    private SocketAcceptThread socketAcceptThread;

    public ServerThreadManager(ServerConfig config) throws IOException {
        this.config = config;
        this.serverService = new InteractiveServerService();
        this.listenServerSocket = SocketUtils.createServerSocket(config.getServerPort());
        log.info("client service [{}] is created!", config.getServerPort());
    }

    public void start() {
        if (this.isCancel.get()) {
            throw new IllegalStateException("已退出，不得重新启动");
        }

        log.info("client service [{}] starting ...", this.config.getServerPort());

        if (!this.isAlive.compareAndSet(false, true)) {
            log.warn("已经启动过了");
            return;
        }

        if (this.config.getNio()) {
            if (this.socketAcceptThread == null || !this.socketAcceptThread.isAlive()) {
                this.socketAcceptThread = new SocketAcceptThread("socket-accept-thread-" + config.getServerPort());
                this.socketAcceptThread.start();
            }
        } else {
            try {
                ServerSocketChannel channel = this.listenServerSocket.getChannel();
                GoThroughNioContainer.register(channel, SelectionKey.OP_ACCEPT, this);
            } catch (IOException e) {
                log.error("register server channel accept port [{}] error", this.config.getServerPort());
                this.cancel();
                throw new RuntimeException("nio注册时异常", e);
            }
        }

        log.info("client service [{}] start success", this.config.getServerPort());
    }

    public void processAcceptClientSocket(Socket acceptClientSocket) {
        executorService.execute(() -> {
            try {
                this.serverService.createClientSocketAdapter().process(acceptClientSocket);
            } catch (Exception e) {
                log.error("处理socket异常", e);
//                try {
//                    acceptClientSocket.close();
//                } catch (IOException ex) {
//                    log.warn("处理新socket时异常，并在关闭socket时异常", ex);
//                }
            }
        });
    }

    public void cancel() {
        if (!this.isCancel.compareAndSet(false, true)) {
            log.warn("已经取消过了");
            return;
        }
        log.info("service [{}] will cancel", this.config.getServerPort());
        if (!this.isAlive.compareAndSet(true, false)) {
            log.warn("已经取消过了");
            return;
        }
        ServerSocket listenServerSocket;
        if ((listenServerSocket = this.listenServerSocket) != null) {
            if (!this.config.getNio()) {
                GoThroughNioContainer.release(listenServerSocket.getChannel());
            }
            try {
                listenServerSocket.close();
            } catch (IOException e) {
                log.warn("监听端口关闭异常", e);
            }
        }

        if (this.socketAcceptThread != null) {
            this.socketAcceptThread.interrupt();
        }

        log.info("server [{}] cancel success", this.config.getServerPort());
    }

    @Override
    public void doProcess(SelectionKey key) {
        if (!key.isValid()) {
            this.cancel();
        }

        try {
            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
            SocketChannel accept = channel.accept();
            for (; Objects.nonNull(accept); accept = channel.accept()) {
                this.processAcceptClientSocket(accept.socket());
            }
        } catch (IOException e) {
            log.warn("客户端服务进程 轮询等待出现异常", e);
            this.cancel();
        }
    }

    class SocketAcceptThread extends Thread {

        public SocketAcceptThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (isAlive.get()) {
                try {
                    Socket acceptClientSocket = listenServerSocket.accept();
                    processAcceptClientSocket(acceptClientSocket);
                } catch (Exception e) {
                    log.warn("客户端服务进程 轮询等待出现异常", e);
                    cancel();
                }
            }
        }

    }
}
