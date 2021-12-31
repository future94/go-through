package com.future94.gothrough.protocol.nio.handler;

import java.nio.channels.SelectionKey;

/**
 * 该处理器是对{@link SelectionKey#OP_READ}事件读到的数据进行处理.
 * @author weilai
 */
@FunctionalInterface
public interface ChannelReadableHandler {

    /**
     * 处理方法
     * @param msg 当{@link SelectionKey#OP_READ}事件准备好时读取到的数据
     */
    void channelRead(Object msg) throws Exception;
}
