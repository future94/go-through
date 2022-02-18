package cn.gothrough.server.boss.handler;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import cn.gothrough.server.context.GoThroughContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Boss服务端消息转发处理器
 * @author weilai
 */
public class DispatcherHandler extends SimpleChannelInboundHandler<BinaryMessage> {

    private static Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

    private static List<MessageHandler> messageHandlerList = new ArrayList<>();

    public DispatcherHandler() {
        messageHandlerList.add(new AuthMessageHandler());
        messageHandlerList.add(new ConnectMessageHandler());
        messageHandlerList.add(new DisconnectMessageHandler());
        messageHandlerList.add(new TransferMessageHandler());
        messageHandlerList.add(new HeartbeatMessageHandler());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryMessage msg) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("receive {}", msg);
        }
        boolean match = false;
        for (MessageHandler messageHandler : messageHandlerList) {
            if (messageHandler.supports(msg.getType())) {
                match = true;
                if (!messageHandler.process(ctx, msg)) {
                    ctx.channel().close();
                }
                break;
            }
        }
        if (!match) {
            logger.warn("未找到消息处理器, type:[{}]", msg.getType());
        }
    }
//
//    @Override
//    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
//        Channel proxyChannel = ctx.channel().attr(AttributeKeyConstants.PROXY_CHANNEL).get();
//        if (proxyChannel != null) {
//            proxyChannel.config().setAutoRead(ctx.channel().isWritable());
//        }
//        super.channelWritabilityChanged(ctx);
//    }

//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        Channel proxyChannel = ctx.channel().attr(AttributeKeyConstants.PROXY_CHANNEL).get();
//        if (proxyChannel != null && proxyChannel.isActive()) {
//            String key = ctx.channel().attr(AttributeKeyConstants.CLIENT_KEY).get();
//            String serverListenId = ctx.channel().attr(AttributeKeyConstants.SERVER_LISTEN_ID).get();
//            Channel clientChannel = GoThroughContext.getClientChannel(key);
//            if (clientChannel != null) {
//                Map<String, Channel> serverListenChannelMapping = clientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL_MAPPING).get();
//                if (serverListenChannelMapping != null) {
//                    serverListenChannelMapping.remove(serverListenId);
//                }
//            }
//            proxyChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//            proxyChannel.close();
//        } else {
//            // TODO ProxyChannelManager.removeCmdChannel(ctx.channel());
//        }
//        super.channelInactive(ctx);
//    }
}
