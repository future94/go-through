package com.future94.gothrough.protocol.nio.thread.server;

import com.future94.gothrough.protocol.nio.thread.IProcess;

import java.io.IOException;

/**
 * 服务端
 * @author weilai
 */
public interface IServer extends IProcess {

    /**
     * 创建服务
     * @throws IOException 创建Server失败
     */
    void create() throws IOException;

    /**
     * 获取监听的接口
     * @return {@code int}  端口
     *         {@code null} 未设置端口
     */
    Integer getPort();

    /**
     * 设置监听的端口
     * @param port 启动监听的端口
     */
    void setPort(int port);

    /**
     * 启动NIO Server
     * @return {@code true} 启动监听程序成功
     * @throws IOException 启动失败
     */
    boolean start() throws IOException;

    /**
     * 启动NIO Server
     * @param port 启动监听的端口
     * @return {@code true} 启动监听程序成功
     * @throws IOException 启动失败
     */
    boolean start(int port) throws IOException;

    /**
     * 是否已经启动
     * @return {@code true} 启动监听程序成功
     */
    boolean isStart();

    /**
     * 停止NIO Server
     */
    void stop();

}
