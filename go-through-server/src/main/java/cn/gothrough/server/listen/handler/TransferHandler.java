package cn.gothrough.server.listen.handler;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.message.BinaryMessage;
import cn.gothrough.server.boss.handler.ConnectMessageHandler;
import cn.gothrough.server.config.ServerConfig;
import cn.gothrough.server.context.GoThroughContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Listen服务端的消息转发处理器
 * @author weilai
 */
public class TransferHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static Logger logger = LoggerFactory.getLogger(TransferHandler.class);

    private static AtomicLong serverListenIdProducer = new AtomicLong();

    /**
     * 向客户端代理发送{@link BinaryMessage#TYPE_TRANSFER}消息转发实际请求的数据
     *
     * 客户端向Boss发送{@link BinaryMessage#TYPE_CONNECT}请求时.
     * 通过{@link ConnectMessageHandler}处理器处理后会将{@link GoThroughContext#setClientProxyChannel(Integer, Channel)}设置clientProxyChannel值.
     * 并将Channel的{@link ChannelOption#AUTO_READ}设置为{@code true}.这时候开始读取数据，会调用到这里.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Channel listenChannel = ctx.channel();
        final Integer listenPort = ((InetSocketAddress) listenChannel.localAddress()).getPort();
        // FIXME 客户端重新启动时这个值取到的还是之前的旧值(因为客户端只发了Auth消息，这个值在Connect消息时设置)
        Channel clientProxyChannel = GoThroughContext.getClientProxyChannel(listenPort);
        if (clientProxyChannel == null) {
            logger.warn("未找到客户端代理channel");
            ctx.channel().close();
            return;
        }
        if (!clientProxyChannel.isActive()) {
            logger.warn("客户端已经断开");
            listenChannel.close();
            return;
        }
        logger.debug("[{}]向clientProxyChannel写入数据开始", listenChannel.toString());
        clientProxyChannel.writeAndFlush(BinaryMessage.buildTransferMessage(listenChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).get(), ByteBufUtil.getBytes(msg)));
        logger.debug("[{}]向clientProxyChannel写入数据结束", listenChannel.toString());
    }

    /**
     * 当有Channel进行Accept时候，设置{@link ChannelOption#AUTO_READ}为{@code false}
     * 当与客户端交互成功并客户端与内部服务建立好连接后，即Boss服务端收到客户端发来的{@link BinaryMessage#TYPE_CONNECT}消息
     * {@link ConnectMessageHandler}处理器会设置{@link ChannelOption#AUTO_READ}为{@code true}，触发{@link #channelRead0(ChannelHandlerContext, ByteBuf)}方法转发消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Server监听到收到数据的Channel
        Channel listenChannel = ctx.channel();
        final Integer listenPort = ((InetSocketAddress) listenChannel.localAddress()).getPort();
        Channel clientChannel = GoThroughContext.getClientChannel(listenPort);
        if (clientChannel == null) {
            logger.warn("未找到连接客户端channel");
            ctx.channel().close();
            return;
        }
        if (GoThroughContext.isConnected(listenPort)) {
            return;
        }
        // 用户连接到代理服务器时，设置用户连接不可读，等待代理后端服务器连接成功后再改变为可读状态
        listenChannel.config().setAutoRead(false);
        // FIXME 当使用连接池的时候，channelActive只会触发一次，导致多个连接池serverListenId重复，如果上次请求转发还没结束时又read到了数据会产生冲突导致阻塞。
        final String serverListenId = genServerListenId();
        logger.debug("channelActive channel [{}], gen serverListenId : [{}]", listenChannel.toString(), serverListenId);
        final String hostName = ServerConfig.getInstance().getClientProxyConfig(listenPort).getHostName();
        listenChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(serverListenId);
        listenChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_HOSTNAME).set(hostName);
        clientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL_MAPPING).get().put(serverListenId, listenChannel);
        clientChannel.writeAndFlush(BinaryMessage.buildConnectMessage(serverListenId, hostName.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel listenChannel = ctx.channel();
        final int listenPort = ((InetSocketAddress) listenChannel.localAddress()).getPort();
        Channel clientChannel = GoThroughContext.getClientChannel(listenPort);
        if (clientChannel == null) {
            logger.warn("未找到连接客户端channel");
            ctx.channel().close();
            return;
        }
        String serverListenId = listenChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).get();
        Map<String, Channel> clientChannelMap = clientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL_MAPPING).get();
        if (clientChannelMap != null) {
            clientChannelMap.remove(serverListenId);
        }
//        Channel proxyChannel = listenChannel.attr(AttributeKeyConstants.PROXY_CHANNEL).get();
//        if (proxyChannel != null && proxyChannel.isActive()) {
//            proxyChannel.attr(AttributeKeyConstants.PROXY_CHANNEL).set(null);
//            proxyChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(null);
//            proxyChannel.config().setAutoRead(true);
//            proxyChannel.writeAndFlush(BinaryMessage.buildDisconnectMessage(serverListenId));
//        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        super.exceptionCaught(ctx, cause);
    }

    private String genServerListenId() {
        return String.valueOf(serverListenIdProducer.incrementAndGet());
    }
}
