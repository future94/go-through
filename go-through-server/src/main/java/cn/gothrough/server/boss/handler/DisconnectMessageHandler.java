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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 接收客户端发来的{@link BinaryMessage#TYPE_DISCONNECT}消息
 *
 * 客户端与内部接口建立连接失败时，会给服务端发送{@link BinaryMessage#TYPE_DISCONNECT}消息，服务端接收到这个消息后会断开真实请求服务端的Channel连接
 * @author weilai
 */
public class DisconnectMessageHandler implements MessageHandler {

    private static Logger logger = LoggerFactory.getLogger(DisconnectMessageHandler.class);

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_DISCONNECT == type;
    }

    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        Channel channel = ctx.channel();
        String key = channel.attr(AttributeKeyConstants.CLIENT_KEY).get();
        String serverListenId = message.getData();
        if (StringUtil.isNullOrEmpty(serverListenId)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Disconnect消息获取ServerListenId失败");
            }
            return false;
        }
        if (StringUtil.isNullOrEmpty(key)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Disconnect消息未获取到ClientKey, serverListenId:[{}]", serverListenId);
            }
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
            if (logger.isWarnEnabled()) {
                logger.warn("Disconnect消息未获取到客户端信息, serverListenId:[{}]", serverListenId);
            }
            return true;
        }
        Map<String, Channel> clientChannelMap = clientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL_MAPPING).get();
        Channel listenChannel = clientChannelMap.remove(serverListenId);
        listenChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        channel.attr(AttributeKeyConstants.PROXY_CHANNEL).set(null);
        channel.attr(AttributeKeyConstants.CLIENT_KEY).set(null);
        channel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(null);
        return true;
    }
}
