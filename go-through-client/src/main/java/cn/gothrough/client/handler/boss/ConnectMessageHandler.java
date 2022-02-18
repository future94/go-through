package cn.gothrough.client.handler.boss;

import cn.gothrough.client.context.GoThroughContext;
import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;

import java.nio.charset.StandardCharsets;

/**
 * 接收Listen服务端发来的{@link BinaryMessage#TYPE_CONNECT}消息
 *
 * 收到消息时，向要暴漏的内网建立连接，
 * 成功向boss服务端发送{@link BinaryMessage#TYPE_CONNECT}消息。
 * 失败向boss服务端发送{@link BinaryMessage#TYPE_DISCONNECT}消息。
 * @author weilai
 */
public class ConnectMessageHandler implements MessageHandler {

    private final Bootstrap intranetBootstrap;

    public ConnectMessageHandler(Bootstrap intranetBootstrap) {
        this.intranetBootstrap = intranetBootstrap;
    }

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_CONNECT == type;
    }

    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        final Channel serverChannel = ctx.channel();;
        final String serverListenId = message.getData();
        if (serverListenId == null) {
            return false;
        }
        String clientHostname = new String(message.getByteBuffer(), StandardCharsets.UTF_8);
        String host;
        int port;
        try {
            String[] split = clientHostname.split(":");
            host = split[0];
            port = Integer.parseInt(split[1]);
        } catch (Exception e) {
            return false;
        }
        intranetBootstrap.connect(host, port).addListener((ChannelFutureListener) (future) -> {
            if (future.isSuccess()) {
                Channel intranetChannel = future.channel();
                intranetChannel.config().setAutoRead(false);
                serverChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_CHANNEL).set(intranetChannel);
                intranetChannel.attr(AttributeKeyConstants.CLIENT_SERVER_CHANNEL).set(serverChannel);
                intranetChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(serverListenId);
                serverChannel.writeAndFlush(BinaryMessage.buildConnectMessage(serverListenId, "clientId".getBytes(StandardCharsets.UTF_8)));
                // 保证读消息的时候已经设置好了attr
                intranetChannel.config().setAutoRead(true);
                GoThroughContext.addIntranetChannel(serverListenId, intranetChannel);
            } else {
                serverChannel.writeAndFlush(BinaryMessage.buildDisconnectMessage(serverListenId));
            }
        });
        return true;
    }
}
