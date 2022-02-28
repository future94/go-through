package cn.gothrough.client.handler.boss;

import cn.gothrough.client.utils.IntranetUtils;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(ConnectMessageHandler.class);

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
        final Channel serverChannel = ctx.channel();
        final String serverListenId = message.getData();
        if (serverListenId == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Connect消息获取serverListenId失败");
            }
            return false;
        }
        String clientHostname = new String(message.getByteBuffer(), StandardCharsets.UTF_8);
        return IntranetUtils.connect(intranetBootstrap, clientHostname, serverChannel, serverListenId);
    }
}
