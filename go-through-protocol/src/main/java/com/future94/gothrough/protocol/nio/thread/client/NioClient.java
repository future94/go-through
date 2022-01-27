package com.future94.gothrough.protocol.nio.thread.client;

import java.nio.channels.SocketChannel;

/**
 * Nio客户端
 * @author weilai
 */
public interface NioClient extends IClient {

    /**
     * 向SocketChannel中写入数据
     * @param msg   要写入的数据
     *              如果msg是byte[]类型不会进行编码
     * @return {@code true} 写入buffer成功
     */
    boolean writeChannel(Object msg);

    /**
     * 获取对应的SocketChannel
     * @return  连接Server的SocketChannel
     */
    SocketChannel getSocketChannel();

}
