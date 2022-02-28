package cn.gothrough.client.handler.intranet;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author weilai
 */
public class TransferHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * 读取内部暴露Channel返回的消息，并向ServerChannel发送{@link BinaryMessage#TYPE_TRANSFER}转发消息
     * @param msg   请求内部要暴露Channel返回的消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // 与内部要暴漏建立连接的Channel
        Channel channel = ctx.channel();
        Channel clientServerChannel = channel.attr(AttributeKeyConstants.CLIENT_SERVER_CHANNEL).get();
        if (clientServerChannel == null) {
            ctx.channel().close();
            return;
        }
        clientServerChannel.writeAndFlush(BinaryMessage.buildTransferMessage(channel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).get(), ByteBufUtil.getBytes(msg)));
    }

//    @Override
//    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
//        Channel channel = ctx.channel();
//        Channel clientServerChannel = channel.attr(AttributeKeyConstants.CLIENT_SERVER_CHANNEL).get();
//        if (clientServerChannel != null) {
//            clientServerChannel.config().setAutoRead(channel.isWritable());
//        }
//        super.channelWritabilityChanged(ctx);
//    }

//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        Channel channel = ctx.channel();
//        String serverListenId = channel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).get();
//        // TODO ClientChannelMannager.removeRealServerChannel(userId);
//        Channel serverChannel = channel.attr(AttributeKeyConstants.PROXY_CHANNEL).get();
//        if (serverChannel != null && serverChannel.isActive()) {
//            serverChannel.writeAndFlush(BinaryMessage.buildDisconnectMessage(serverListenId));
//        }
//        super.channelInactive(ctx);
//    }
}
