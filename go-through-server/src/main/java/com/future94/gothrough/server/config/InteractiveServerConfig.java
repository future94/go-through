package com.future94.gothrough.server.config;

import lombok.Data;

/**
 * @author weilai
 */
@Data
public class InteractiveServerConfig implements ServerConfig {

    private static final int DEFAULT_SERVER_PORT = 19507;

    private static final boolean DEFAULT_NIO = true;

    private Integer serverPort;

    private Boolean nio;

    public InteractiveServerConfig() {
        this(DEFAULT_SERVER_PORT);
    }

    public InteractiveServerConfig(boolean nio) {
        this(DEFAULT_SERVER_PORT, nio);
    }

    public InteractiveServerConfig(Integer serverPort) {
        this(serverPort, DEFAULT_NIO);
    }

    public InteractiveServerConfig(Integer serverPort, Boolean nio) {
        this.serverPort = serverPort;
        this.nio = nio;
    }
}
