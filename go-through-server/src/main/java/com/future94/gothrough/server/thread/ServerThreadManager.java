package com.future94.gothrough.server.thread;

import com.future94.gothrough.protocol.nio.server.GoThroughNioServer;
import com.future94.gothrough.protocol.nio.server.NioServer;
import com.future94.gothrough.server.config.ServerConfig;
import com.future94.gothrough.server.handler.ClientConnectChannelReadableHandler;
import com.future94.gothrough.server.handler.ClientControlChannelReadableHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author weilai
 */
@Slf4j
public class ServerThreadManager {

    private volatile boolean isAlive = false;

    private volatile boolean isCancel = false;

    private final ServerConfig serverConfig;

    private final NioServer server;

    public ServerThreadManager(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        GoThroughNioServer nioServer = new GoThroughNioServer();
        nioServer.setPort(serverConfig.getServerPort());
        nioServer.setReadableHandler(new ClientControlChannelReadableHandler());
        nioServer.setReadableHandler(new ClientConnectChannelReadableHandler());
//        nioServer.setWritableHandler();
        this.server = nioServer;
    }

    public void start() throws IOException {
        if (this.isCancel) {
            throw new IllegalStateException("已退出，不得重新启动");
        }
        log.info("client service [{}] starting ...", this.serverConfig.getServerPort());
        if (this.isAlive) {
            log.warn("已经启动过了");
            return;
        }
        this.isAlive = true;
        this.server.start();
        log.info("client service [{}] start success", this.serverConfig.getServerPort());
    }
}
