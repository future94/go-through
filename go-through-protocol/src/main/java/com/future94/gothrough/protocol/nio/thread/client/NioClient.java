package com.future94.gothrough.protocol.nio.thread.client;

import java.nio.channels.SocketChannel;

/**
 * @author weilai
 */
public interface NioClient extends IClient {

    boolean writeChannel(Object msg);

    SocketChannel getSocketChannel();

}
