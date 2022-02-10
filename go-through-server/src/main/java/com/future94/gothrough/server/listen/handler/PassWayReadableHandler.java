package com.future94.gothrough.server.listen.handler;

import com.future94.gothrough.protocol.nio.handler.SimpleChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author weilai
 */
@Slf4j
public class PassWayReadableHandler extends SimpleChannelReadableHandler<byte[]> {

    private final ServerListenThreadManager manager;

    public PassWayReadableHandler(ServerListenThreadManager manager) {
        this.manager = manager;
    }

    @Override
    protected boolean support(byte[] msg) {
        return true;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) {

    }
}
