package com.future94.gothrough.server.listen.config;

/**
 * @author weilai
 */
public interface ServerListenConfig {

    Integer getListenPort();

    /**
     * 是否使用Thread NIO
     * false 则使用 java New IO
     */
    Boolean getNio();
}
