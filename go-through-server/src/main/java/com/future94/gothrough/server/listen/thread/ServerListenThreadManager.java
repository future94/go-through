package com.future94.gothrough.server.listen.thread;

import com.future94.gothrough.protocol.nio.server.GoThroughNioServer;
import com.future94.gothrough.protocol.nio.server.NioServer;
import com.future94.gothrough.protocol.part.SocketPart;
import com.future94.gothrough.protocol.thread.ThreadManager;
import com.future94.gothrough.server.listen.cache.ServerListenThreadCache;
import com.future94.gothrough.server.listen.config.ServerListenConfig;
import com.future94.gothrough.server.listen.handler.ClientWaitAcceptHandler;
import com.future94.gothrough.server.listen.handler.CommonReplyReadableHandler;
import com.future94.gothrough.server.listen.handler.HeartBeatReadableHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilai
 */
@Slf4j
public class ServerListenThreadManager implements ThreadManager {

    /**
     * 是否运行
     */
    private volatile boolean isAlive = false;

    /**
     * 是否取消
     */
    private volatile boolean isCancel = false;

    /**
     * 监听配置
     */
    private final ServerListenConfig config;

    /**
     * NIO服务端
     */
    private final NioServer server;

    /**
     * SocketPart缓存
     * String：socketPartKey
     */
    private final Map<String, SocketPart> socketPartCache = new ConcurrentHashMap<>();

    public ServerListenThreadManager(ServerListenConfig config) {
        this.config = config;
        GoThroughNioServer nioServer = new GoThroughNioServer();
        nioServer.setPort(config.getListenPort());
        nioServer.setAcceptHandler(new ClientWaitAcceptHandler(this));
        nioServer.setReadableHandler(new CommonReplyReadableHandler());
        nioServer.setReadableHandler(new HeartBeatReadableHandler());
        this.server = nioServer;
        ServerListenThreadCache.remove(this.getListenPort());
        ServerListenThreadCache.add(this);
        log.info("server listen port[{}] is created!", this.getListenPort());
    }

    public Integer getListenPort() {
        return this.config.getListenPort();
    }

    public void setSocketPartCache(String socketPartKey, SocketPart socketPart) {
        this.socketPartCache.put(socketPartKey, socketPart);
    }

    public void write(SocketChannel socketChannel, Object msg) throws Exception {
        server.writeChannel(socketChannel, msg);
    }

    public void start() throws IOException {
        if (this.isCancel) {
            throw new IllegalStateException("已退出，不得重新启动");
        }
        if (this.isAlive) {
            throw new IllegalStateException("已经启动过了");
        }
        this.isAlive = true;
        this.server.start();
        log.info("setControlSocket[{}]", this.getListenPort());

    }

    /**
     * * 退出
     */
    public void cancel() {
        if (this.isAlive) {
            log.warn("已经推出过了");
            return;
        }

        log.info("serverListen cancelling[{}]", this.config.getListenPort());

        ServerListenThreadCache.remove(this.config.getListenPort());

        this.stop();

        try {
            this.server.stop();
        } catch (Exception e) {
            // do no thing
        }

        // TODO ClearInvalidSocketPartThread

        String[] socketPartKeyArray = this.socketPartCache.keySet().toArray(new String[0]);
        for (String key : socketPartKeyArray) {
            this.stopSocketPart(key);
        }
        this.isAlive = true;
        log.debug("serverListen cancel[{}] is success", this.getListenPort());
    }

    /**
     * * 关停监听服务，不注销已经建立的，并置空controlSocket
     */
    public void stop() {
        log.info("stopListen[{}]", this.config.getListenPort());
        if (!isAlive) {
            log.warn("已经停止过了");
            return;
        }


        // TODO
    }

    public boolean doSetPartClient(String socketPartKey, SocketChannel sendSocketChannel) {
        log.debug("接入接口 doSetPartClient[{}]", socketPartKey);
        SocketPart socketPart = this.socketPartCache.get(socketPartKey);
        if (socketPart == null) {
            return false;
        }
        socketPart.setSendSocket(sendSocketChannel);

        boolean createPassWay = socketPart.createPassWay();
        if (!createPassWay) {
            socketPart.cancel();
            this.stopSocketPart(socketPartKey);
            return false;
        }

        return true;
    }

    public void clearInvalidSocketPart() {
        log.debug("clearInvalidSocketPart[{}]", this.getListenPort());
        Iterator<SocketPart> iterator = this.socketPartCache.values().iterator();
        if (iterator.hasNext()) {
            final SocketPart socketPart = iterator.next();
            if (socketPart != null && !socketPart.isValid()) {
                iterator.remove();
                socketPart.cancel();
            }
        }
    }

    public void stopSocketPart(String socketPartKey) {
        log.debug("停止接口 stopSocketPart[{}]", socketPartKey);
        SocketPart socketPart = this.socketPartCache.remove(socketPartKey);
        if (socketPart == null) {
            log.warn("停止接口 stopSocketPart[{}] 为null", socketPartKey);
            return;
        }
        socketPart.cancel();
    }

}
