package com.future94.gothrough.protocol.nio.handler;

import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;

/**
 * 打印接到的数据并原封回写
 * @author weilai
 */
public class TestPrintWriteBackChannelReadableHandler extends SimpleChannelReadableHandler<String> {

    @Override
    protected boolean support(String msg) {
        return true;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println(msg);
        ctx.write(msg);
    }
}
