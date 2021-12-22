package com.future94.gothrough.server.listen.config;

import lombok.Data;

/**
 * @author weilai
 */
@Data
public class InteractiveServerListenConfig implements ServerListenConfig {

    private Integer listenPort;

    private Boolean nio;

    public InteractiveServerListenConfig(Integer listenPort) {
        this(listenPort, true);
    }

    public InteractiveServerListenConfig(Integer listenPort, Boolean nio) {
        this.listenPort = listenPort;
        this.nio = nio;
    }
}
