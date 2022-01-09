package com.future94.gothrough.protocol.nio.handler;

import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;

import java.nio.channels.SelectionKey;

/**
 * 该处理器是对{@link SelectionKey#OP_READ}事件读到的数据进行处理.
 * @author weilai
 */
public interface ChannelReadableHandler {

    boolean supports(Object msg);

    /**
     * 处理方法
     * @param msg 当{@link SelectionKey#OP_READ}事件准备好时读取到的数据
     */
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;
}
