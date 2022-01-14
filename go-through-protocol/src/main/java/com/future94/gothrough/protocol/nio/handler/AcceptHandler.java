package com.future94.gothrough.protocol.nio.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 当{@link java.nio.channels.SelectionKey#OP_ACCEPT}事件就绪的处理器
 * @author weilai
 */
@FunctionalInterface
public interface AcceptHandler {

    /**
     * 处理方法
     * @param socketChannel 当{@link java.nio.channels.SelectionKey#OP_ACCEPT}事件准备好时的{@link java.nio.channels.SelectionKey}
     */
    void accept(SocketChannel socketChannel) throws IOException;
}
