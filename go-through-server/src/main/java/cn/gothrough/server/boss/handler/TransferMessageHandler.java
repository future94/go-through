package cn.gothrough.server.boss.handler;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理客户端发送过来的{@link BinaryMessage#TYPE_TRANSFER}
 * @author weilai
 */
public class TransferMessageHandler implements MessageHandler {

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_TRANSFER == type;
    }

    /**
     * 将客户端转发过来的数据写入ListenChannel实现转发
     */
    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        Channel clientChannel = ctx.channel();
        Channel listenChannel = clientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL).get();
        if (listenChannel == null) {
            return false;
        }
        ByteBuf buffer = ctx.alloc().buffer(message.getByteBuffer().length);
        buffer.writeBytes(message.getByteBuffer());
        listenChannel.writeAndFlush(buffer);
        return true;
    }
}
