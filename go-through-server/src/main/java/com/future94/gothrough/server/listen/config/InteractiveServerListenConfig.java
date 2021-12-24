package com.future94.gothrough.server.listen.config;

import lombok.Data;

/**
 * @author weilai
 */
@Data
public class InteractiveServerListenConfig implements ServerListenConfig {

    private static final int DEFAULT_SERVER_PORT = 19507;

    private static final boolean DEFAULT_NIO = true;

    private Integer listenPort;

    private Boolean nio;

    public InteractiveServerListenConfig(Integer listenPort) {
        this(listenPort, DEFAULT_NIO);
    }

    public InteractiveServerListenConfig(Integer listenPort, Boolean nio) {
        this.listenPort = listenPort;
        this.nio = nio;
    }
}
