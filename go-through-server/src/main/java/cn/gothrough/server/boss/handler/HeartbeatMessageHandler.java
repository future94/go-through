package cn.gothrough.server.boss.handler;

import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author weilai
 */
public class HeartbeatMessageHandler implements MessageHandler {

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_HEARTBEAT == type;
    }

    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        ctx.channel().writeAndFlush(BinaryMessage.buildHeartbeatMessage());
        return true;
    }
}
