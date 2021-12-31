package com.future94.gothrough.protocol.nio.handler;

import java.nio.channels.SelectionKey;

/**
 * 该处理器是返回当{@link SelectionKey#OP_WRITE}事件准备就绪时要写入{@link java.nio.channels.SocketChannel}的数据.
 * @author weilai
 */
@FunctionalInterface
public interface ChannelWritableHandler {

    /**
     * 处理方法
     * @return 要写入的数据
     */
    Object channelWrite() throws Exception;
}
