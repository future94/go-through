package com.future94.gothrough.client.config;

/**
 * @author weilai
 */
public interface ClientConfig {

    /**
     * 是否使用Thread NIO
     * false 则使用 java New IO
     */
    Boolean getNio();

    /**
     * 服务端IP地址
     */
    String getServerIp();

    /**
     * 服务端端口
     */
    Integer getServerPort();

    /**
     * 要暴露服务端监听的端口
     */
    Integer getServerExposedListenPort();

    /**
     * 要暴露的内网IP
     */
    String getExposedIntranetIp();

    /**
     * 要暴露的内网端口
     */
    Integer getExposedIntranetPort();
}
