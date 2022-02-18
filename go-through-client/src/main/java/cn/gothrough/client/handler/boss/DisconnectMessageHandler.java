package cn.gothrough.client.handler.boss;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * @author weilai
 */
public class DisconnectMessageHandler implements MessageHandler {

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_DISCONNECT == type;
    }

    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        Attribute<Channel> proxyAttr = ctx.channel().attr(AttributeKeyConstants.PROXY_CHANNEL);
        Channel proxyChannel = proxyAttr.get();
        if (proxyChannel != null) {
            proxyAttr.set(null);
            proxyChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        return false;
    }

}
