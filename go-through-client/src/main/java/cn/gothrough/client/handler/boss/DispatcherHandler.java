package cn.gothrough.client.handler.boss;

import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author weilai
 */
public class DispatcherHandler extends SimpleChannelInboundHandler<BinaryMessage> {

    private static Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

    private static List<MessageHandler> messageHandlerList = new ArrayList<>();

    public DispatcherHandler(Bootstrap intranetBootstrap) {
        messageHandlerList.add(new HeartbeatMessageHandler());
        messageHandlerList.add(new ConnectMessageHandler(intranetBootstrap));
        messageHandlerList.add(new DisconnectMessageHandler());
        messageHandlerList.add(new TransferMessageHandler(intranetBootstrap));
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
                    if (logger.isDebugEnabled()) {
                        logger.debug("[{}]处理[{}]失败", messageHandler.getClass().getSimpleName(), msg);
                    }
                    ctx.channel().close();
                }
                break;
            }
        }
        if (!match) {
            logger.warn("未找到消息处理器, type:[{}]", msg.getType());
        }
    }

//    @Override
//    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
//        Channel proxyChannel = ctx.channel().attr(AttributeKeyConstants.PROXY_CHANNEL).get();
//        if (proxyChannel != null) {
//            proxyChannel.config().setAutoRead(ctx.channel().isWritable());
//        }
//        super.channelWritabilityChanged(ctx);
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        if (GoThroughContext.getBossServerChannel() == ctx.channel()) {
//            GoThroughContext.setBossServerChannel(null);
//            GoThroughContext.clearIntranetChannels();
//            // TODO 重新连接
//        } else {
//            Channel intranetChannel = ctx.channel().attr(AttributeKeyConstants.PROXY_CHANNEL).get();
//            if (intranetChannel != null && intranetChannel.isActive()) {
//                intranetChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//            }
//        }
//        super.channelInactive(ctx);
//    }
}
