package cn.gothrough.server.boss.handler;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import cn.gothrough.server.context.GoThroughContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

/**
 * 处理客户度发送过来的{@link BinaryMessage#TYPE_CONNECT}
 * @author weilai
 */
public class ConnectMessageHandler implements MessageHandler {

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_CONNECT == type;
    }

    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        Channel proxyClientChannel = ctx.channel();;
        final String serverListenId = message.getData();
        if (serverListenId == null) {
            return false;
        }
        String key = new String(message.getByteBuffer(), StandardCharsets.UTF_8);
        Channel clientChannel = GoThroughContext.getClientChannel(key);
        Channel listenChannel = clientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL_MAPPING).get().get(serverListenId);
        if (listenChannel == null) {
            return false;
        }
        proxyClientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(serverListenId);
        proxyClientChannel.attr(AttributeKeyConstants.CLIENT_KEY).set(key);
        proxyClientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL).set(listenChannel);
        listenChannel.attr(AttributeKeyConstants.CLIENT_PROXY_CHANNEL).set(proxyClientChannel);
        listenChannel.config().setAutoRead(true);
        return true;
    }
}
