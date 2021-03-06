package com.future94.gothrough.server.listen.thread;

import com.future94.gothrough.common.utils.SequenceUtils;
import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.part.SocketPart;
import com.future94.gothrough.protocol.thread.AbstractNIORunnable;
import com.future94.gothrough.protocol.thread.GoThroughNioContainer;
import com.future94.gothrough.protocol.thread.GoThroughNIORunnable;
import com.future94.gothrough.protocol.thread.GoThroughThreadFactory;
import com.future94.gothrough.protocol.thread.ThreadManager;
import com.future94.gothrough.server.listen.cache.ServerListenThreadCache;
import com.future94.gothrough.server.listen.clear.ClearInvalidSocketPartThread;
import com.future94.gothrough.server.listen.config.ServerListenConfig;
import com.future94.gothrough.server.listen.manager.SocketManager;
import com.future94.gothrough.server.listen.service.ListenServerService;
import com.future94.gothrough.server.listen.service.impl.InteractiveListenServerService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author weilai
 */
@Slf4j
public class ServerListenThreadManager extends AbstractNIORunnable implements ThreadManager {

    private AtomicBoolean isAlive = new AtomicBoolean(false);

    private AtomicBoolean isCancel = new AtomicBoolean(false);

    private SocketManager socketManager;

    private SocketAcceptThread socketAcceptThread;

    private final ServerListenConfig config;

    private final ServerSocket listenServerSocket;

    private final ListenServerService listenServerService;

    private ClearInvalidSocketPartThread clearInvalidSocketPartThread;

    private final ExecutorService executorService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), GoThroughThreadFactory.create("socket-accept-work"), new ThreadPoolExecutor.CallerRunsPolicy());

    private final Map<String, SocketPart> socketPartMap = new ConcurrentHashMap<>();

    public ServerListenThreadManager(ServerListenConfig config) throws IOException {
        this.config = config;
        this.listenServerService = new InteractiveListenServerService();
        this.listenServerSocket = SocketUtils.createServerSocket(config.getListenPort());
        ServerListenThreadCache.remove(this.getListenPort());
        ServerListenThreadCache.add(this);
        log.info("server listen port[{}] is created!", this.getListenPort());
    }

    /**
     * ??????????????????
     */
    private void sendTask(Socket acceptClientSocket) {
        executorService.execute(() -> {
            // ????????????????????????socket?????????????????????????????????????????????????????????controlSocket??????????????????????????????controlSocket???????????????
            if (Objects.isNull(this.socketManager)) {
                try {
                    acceptClientSocket.close();
                } catch (IOException e) {
                    // do nothing
                }
                return;
            }

            String socketPartKey = SequenceUtils.genSocketPartKey(this.config.getListenPort());

            SocketPart socketPart = this.listenServerService.createSocketPart(this);
            socketPart.setSocketPartKey(socketPartKey);
            socketPart.setRecvSocket(acceptClientSocket);

            this.socketPartMap.put(socketPartKey, socketPart);
            // ????????????????????????controlSocket????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (!this.sendClientWait(socketPartKey)) {
                this.socketPartMap.remove(socketPartKey);
                socketPart.cancel();
            }
        });
    }

    private boolean sendClientWait(String socketPartKey) {
        log.info("??????????????? sendClientWait[{}]", socketPartKey);
        boolean sendClientWait = false;

        try {
            sendClientWait = this.socketManager.sendServerWaitClient(socketPartKey);
        } catch (Throwable e) {
            log.error("??????????????? sendClientWait[" + socketPartKey + "] ??????????????????", e);
        }

        if (!sendClientWait) {
            log.warn("??????????????? sendClientWait[" + socketPartKey + "] ??????");
            if (this.socketManager == null || !this.socketManager.isValid()) {
                // ??????control???????????????
                this.stopListen();
            }
            return false;
        }
        return true;
    }

    public void controlCloseNotice(SocketManager socketManager) {
        if (Objects.equals(socketManager, this.socketManager)) {
            this.stopListen();
        }
    }

    /**
     * * ?????????????????????????????????????????????????????????controlSocket
     */
    private void stopListen() {
        log.info("stopListen[{}]", this.config.getListenPort());
        if (!this.isAlive.compareAndSet(true, false)) {
            log.warn("??????????????????");
        }

        if (this.getNio()) {
            if (this.socketAcceptThread != null) {
                this.socketAcceptThread.interrupt();
                // help gc
                this.socketAcceptThread = null;
            }
        } else {
            GoThroughNioContainer.release(this.listenServerSocket.getChannel());
        }

        if (this.socketManager != null) {
            try {
                this.socketManager.close();
            } catch (Exception e) {
                log.error("????????????????????????????????????", e);
            }
            // help gc
            this.socketManager = null;
        }
    }

    public void start(Socket socket) {
        if (this.isCancel.get()) {
            throw new IllegalStateException("??????????????????????????????");
        }

        if (!this.isAlive.compareAndSet(false, true)) {
            throw new IllegalStateException("??????????????????");
        }

        log.info("setControlSocket[{}]", this.getListenPort());

        SocketManager controlSocketNew = this.listenServerService.createSocketManager(socket);

        if (this.socketManager != null) {
            try {
                this.socketManager.close();
            } catch (Exception e) {
                log.debug("????????????????????????????????????", e);
            }
            this.socketManager = null;
        }
        controlSocketNew.setServerListenThreadManager(this);
        controlSocketNew.startRecv();
        this.socketManager = controlSocketNew;

        log.info("server listen port[{}] starting ...", this.getListenPort());

        if (this.clearInvalidSocketPartThread == null) {
            this.clearInvalidSocketPartThread = this.listenServerService.createClearInvalidSocketPartThread(this);
            this.clearInvalidSocketPartThread.start();
        }

        if (getNio()) {
            if (this.socketAcceptThread == null || !this.socketAcceptThread.isAlive()) {
                this.socketAcceptThread = new SocketAcceptThread("server-listen-" + this.getListenPort());
                this.socketAcceptThread.start();
            }
        } else {
            try {
                ServerSocketChannel channel = this.listenServerSocket.getChannel();
                GoThroughNioContainer.register(channel, SelectionKey.OP_ACCEPT, this);
            } catch (IOException e) {
                log.error("register serverListen channel[{}] faild!", config.getListenPort());
                this.cancel();
                throw new RuntimeException("nio???????????????", e);
            }
        }

        log.info("server listen port[{}] start success!", this.getListenPort());
    }

    public Integer getListenPort() {
        return this.config.getListenPort();
    }

    @Override
    public Boolean getNio() {
        return this.config.getNio();
    }

    /**
     * * ??????
     */
    public void cancel() {
        if (!this.isCancel.compareAndSet(false, true)) {
            log.warn("??????????????????");
            return;
        }

        log.info("serverListen cancelling[{}]", this.config.getListenPort());

        ServerListenThreadCache.remove(this.config.getListenPort());

        this.stopListen();

        try {
            this.listenServerSocket.close();
        } catch (Exception e) {
            // do no thing
        }

        if (this.clearInvalidSocketPartThread != null) {
            try {
                clearInvalidSocketPartThread.cancel();
            } catch (Exception e) {
                // do no thing
            }
            this.clearInvalidSocketPartThread = null;
        }

        String[] socketPartKeyArray = this.socketPartMap.keySet().toArray(new String[0]);
        for (String key : socketPartKeyArray) {
            this.stopSocketPart(key);
        }

        log.debug("serverListen cancel[{}] is success", this.getListenPort());
    }

    public boolean doSetPartClient(String socketPartKey, Socket sendSocket) {
        log.debug("???????????? doSetPartClient[{}]", socketPartKey);
        SocketPart socketPart = this.socketPartMap.get(socketPartKey);
        if (socketPart == null) {
            return false;
        }
        socketPart.setSendSocket(sendSocket);

        boolean createPassWay = socketPart.createPassWay();
        if (!createPassWay) {
            socketPart.cancel();
            this.stopSocketPart(socketPartKey);
            return false;
        }

        return true;
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
                this.sendTask(accept.socket());
            }
        } catch (IOException e) {
            log.warn("????????????[" + this.getListenPort() + "]????????????", e);
            this.cancel();
        }
    }

    public void clearInvalidSocketPart() {
        log.debug("clearInvalidSocketPart[{}]", this.getListenPort());

        Set<String> keySet = this.socketPartMap.keySet();
        // ??????????????????set??????????????????????????????
        String[] array = keySet.toArray(new String[0]);

        for (String key : array) {
            SocketPart socketPart = this.socketPartMap.get(key);
            if (socketPart != null && !socketPart.isValid()) {
                this.stopSocketPart(key);
            }
        }

    }

    @Override
    public void stopSocketPart(String socketPartKey) {
        log.debug("???????????? stopSocketPart[{}]", socketPartKey);
        SocketPart socketPart = this.socketPartMap.remove(socketPartKey);
        if (socketPart == null) {
            log.warn("???????????? stopSocketPart[{}] ???null", socketPartKey);
            return;
        }
        socketPart.cancel();
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
                    sendTask(acceptClientSocket);
                } catch (Exception e) {
                    log.warn("????????????[" + config.getListenPort() + "]????????????", e);
                    cancel();
                }
            }
        }

    }

}
