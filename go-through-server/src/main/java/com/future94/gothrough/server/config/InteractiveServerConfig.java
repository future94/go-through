package com.future94.gothrough.server.config;

import lombok.Data;

/**
 * @author weilai
 */
@Data
public class InteractiveServerConfig implements ServerConfig {

    private Integer serverPort;

    private Boolean nio;

    public InteractiveServerConfig() {
        this(19507);
    }

    public InteractiveServerConfig(Integer serverPort) {
        this(serverPort, true);
    }

    public InteractiveServerConfig(Integer serverPort, Boolean nio) {
        this.serverPort = serverPort;
        this.nio = nio;
    }
}
