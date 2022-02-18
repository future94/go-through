package cn.gothrough.server.boss.handler;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import cn.gothrough.server.config.ServerConfig;
import cn.gothrough.server.context.GoThroughContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理客户端发送来的{@link BinaryMessage#TYPE_AUTH}消息
 * @author weilai
 */
public class AuthMessageHandler implements MessageHandler {

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_AUTH == type;
    }

    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        final Channel clientChannel = ctx.channel();
        String key = message.getData();
        List<Integer> proxyPortList = ServerConfig.getInstance().proxyPortList(key);
        if (proxyPortList == null || proxyPortList.isEmpty()) {
            return false;
        }
        Channel channel = GoThroughContext.getClientChannel(key);
        if (channel != null) {
            return false;
        }
        clientChannel.attr(AttributeKeyConstants.PROXY_PORT).set(proxyPortList);
        clientChannel.attr(AttributeKeyConstants.CLIENT_KEY).set(key);
        clientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL_MAPPING).set(new ConcurrentHashMap<>());
        for (Integer port : proxyPortList) {
            GoThroughContext.setClientChannel(port, clientChannel);
        }
        GoThroughContext.setClientChannel(key, clientChannel);
        return true;
    }
}
