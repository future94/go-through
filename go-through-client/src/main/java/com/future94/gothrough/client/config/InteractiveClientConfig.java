package com.future94.gothrough.client.config;

import lombok.Data;

/**
 * @author weilai
 */
@Data
public class InteractiveClientConfig implements ClientConfig {

    private static final boolean DEFAULT_NIO = true;

    private Boolean nio;

    private String serverIp;

    private Integer serverPort;

    private Integer serverExposedListenPort;

    private String exposedIntranetIp;

    private Integer exposedIntranetPort;

    public InteractiveClientConfig() {
        this(DEFAULT_NIO);
    }

    public InteractiveClientConfig(Boolean nio) {
        this.nio = nio;
    }
}
