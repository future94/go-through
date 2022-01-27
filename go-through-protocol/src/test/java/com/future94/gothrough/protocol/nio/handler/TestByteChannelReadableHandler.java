package com.future94.gothrough.protocol.nio.handler;

import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

/**
 * 打印接到的数据
 * @author weilai
 */
public class TestByteChannelReadableHandler extends SimpleChannelReadableHandler<byte[]> {

    @Override
    protected boolean support(byte[] msg) {
        return true;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) {
        System.out.println(new String(msg, StandardCharsets.UTF_8));
    }
}
