package com.future94.gothrough.server.config;

/**
 * @author weilai
 */
public interface ServerConfig {

    /**
     * 服务端端口
     */
    Integer getServerPort();

    /**
     * 是否使用Thread NIO
     * false 则使用 java New IO
     */
    Boolean getNio();
}
