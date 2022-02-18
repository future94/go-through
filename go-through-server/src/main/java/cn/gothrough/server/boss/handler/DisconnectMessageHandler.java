package cn.gothrough.server.boss.handler;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import cn.gothrough.server.context.GoThroughContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;

import java.util.Map;

/**
 * 接收客户端发来的{@link BinaryMessage#TYPE_DISCONNECT}消息
 *
 * 客户端与内部接口建立连接失败时，会给服务端发送{@link BinaryMessage#TYPE_DISCONNECT}消息，服务端接收到这个消息后会断开真实请求服务端的Channel连接
 * @author weilai
 */
public class DisconnectMessageHandler implements MessageHandler {

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_DISCONNECT == type;
    }

    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        Channel channel = ctx.channel();
        String key = channel.attr(AttributeKeyConstants.CLIENT_KEY).get();
        if (StringUtil.isNullOrEmpty(key)) {
            String serverListenId = message.getData();
            Map<String, Channel> clientChannelMap = channel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL_MAPPING).get();
            Channel clientChannel = clientChannelMap.remove(serverListenId);
            if (clientChannel != null) {
                // 清空buffer & 异步关闭
                clientChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
            return true;
        }
        Channel clientChannel = GoThroughContext.getClientChannel(key);
        if (clientChannel == null) {
            return true;
        }
        Map<String, Channel> clientChannelMap = channel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL_MAPPING).get();
        Channel listenChannel = clientChannelMap.remove(channel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).get());
        listenChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        channel.attr(AttributeKeyConstants.PROXY_CHANNEL).set(null);
        channel.attr(AttributeKeyConstants.CLIENT_KEY).set(null);
        channel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(null);
        return true;
    }
}
