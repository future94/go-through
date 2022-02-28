package cn.gothrough.server.boss.handler;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import cn.gothrough.server.context.GoThroughContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * 处理客户端发送过来的{@link BinaryMessage#TYPE_CONNECT}
 * @author weilai
 */
public class ConnectMessageHandler implements MessageHandler {

    private static Logger logger = LoggerFactory.getLogger(AuthMessageHandler.class);

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_CONNECT == type;
    }

    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        Channel proxyClientChannel = ctx.channel();
        final String serverListenId = message.getData();
        if (serverListenId == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Connect消息获取ServerListenId失败");
            }
            return false;
        }
        String key = new String(message.getByteBuffer(), StandardCharsets.UTF_8);
        Channel clientChannel = GoThroughContext.getClientChannel(key);
        Channel listenChannel = clientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL_MAPPING).get().get(serverListenId);
        if (listenChannel == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Connect消息未找到对应的ListenChannel, ServerListenId:[{}]", serverListenId);
            }
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Connect消息设置ListenChannel:[{}]为可读状态", listenChannel.toString());
        }
        int listenPort = ((InetSocketAddress) listenChannel.localAddress()).getPort();
        proxyClientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(serverListenId);
        proxyClientChannel.attr(AttributeKeyConstants.CLIENT_KEY).set(key);
        proxyClientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL).set(listenChannel);
        GoThroughContext.setClientProxyChannel(listenPort, proxyClientChannel);
        listenChannel.config().setAutoRead(true);
        GoThroughContext.setConnected(listenPort, true);
        return true;
    }
}
