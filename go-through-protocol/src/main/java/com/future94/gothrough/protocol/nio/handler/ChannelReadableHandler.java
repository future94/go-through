package com.future94.gothrough.protocol.nio.handler;

import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;

import java.nio.channels.SelectionKey;

/**
 * 该处理器是对{@link SelectionKey#OP_READ}事件读到的数据进行处理.
 * @author weilai
 */
public interface ChannelReadableHandler {

    /**
     * 该处理器是否能处理该数据
     * @param msg               要处理的数据
     * @return {@code true}     支持
     */
    boolean supports(Object msg);

    /**
     * 处理方法
     * @param msg           当{@link SelectionKey#OP_READ}事件准备好时读取到的数据
     * @throws Exception    写入失败
     */
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;
}
