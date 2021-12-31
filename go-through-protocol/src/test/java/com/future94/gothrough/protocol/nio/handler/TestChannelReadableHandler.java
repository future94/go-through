package com.future94.gothrough.protocol.nio.handler;

/**
 * @author weilai
 */
public class TestChannelReadableHandler extends SimpleChannelReadableHandler<String> {

    @Override
    protected void channelRead0(String msg) {
        System.out.println(msg);
    }
}
