package cn.gothrough.client.handler.boss;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理Listen服务端发送过来的{@link BinaryMessage#TYPE_TRANSFER}消息
 * @author weilai
 */
public class TransferMessageHandler implements MessageHandler {

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_TRANSFER == type;
    }

    /**
     * 将收到的服务端数据转发代理Channel
     */
    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        Channel clientIntranetChannel = ctx.channel().attr(AttributeKeyConstants.CLIENT_INTRANET_CHANNEL).get();
        if (clientIntranetChannel != null) {
            ByteBuf buffer = ctx.alloc().buffer(message.getByteBuffer().length);
            buffer.writeBytes(message.getByteBuffer());
            clientIntranetChannel.writeAndFlush(buffer);
            return true;
        }
        return false;
    }
}
