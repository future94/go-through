package com.future94.gothrough.protocol.nio.thread.client;

import com.future94.gothrough.protocol.nio.thread.IProcess;

import java.io.IOException;

/**
 * @author weilai
 */
public interface IClient extends IProcess {

    /**
     * 连接Server
     * @throws IOException  链接Server失败
     */
    void connect() throws IOException;

    /**
     * 连接Server
     * @param ip            IP
     * @param port          端口
     * @throws IOException  链接Server失败
     */
    void connect(String ip, int port) throws IOException;

    /**
     * 获取连接的Server接口
     * @return {@code String}  IP
     *         {@code null} 未设置IP
     */
    String getIp();

    /**
     * 设置连接的Server端口
     * @param ip 连接的Server的IP
     */
    void setIp(String ip);

    /**
     * 获取连接的Server接口
     * @return {@code int}  端口
     *         {@code null} 未设置端口
     */
    int getPort();

    /**
     * 设置连接的Server端口
     * @param port 连接的Server端口
     */
    void setPort(int port);

    /**
     * 设置已经连接状态
     */
    void setConnected();

    /**
     * 是否已经连接
     * @return {@code true} 连接Server成功
     */
    boolean isConnected();

    /**
     * 是否已经启动
     * @return {@code true} 启动Thread成功
     */
    boolean isStarted();

    /**
     * 断开连接Server
     */
    void close() throws IOException;
}
