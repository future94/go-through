package com.future94.gothrough.protocol.nio.handler;

/**
 * 打印接到的数据
 * @author weilai
 */
public class TestPrintChannelReadableHandler extends SimpleChannelReadableHandler<String> {

    @Override
    protected void channelRead0(String msg) {
        System.out.println(msg);
    }
}
