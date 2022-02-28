package cn.gothrough.client.utils;

import cn.gothrough.client.context.GoThroughContext;
import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author weilai
 */
public class IntranetUtils {

    private static Logger logger = LoggerFactory.getLogger(IntranetUtils.class);

    /**
     * 向要暴漏端口建立连接
     */
    public static boolean connect(Bootstrap intranetBootstrap, String clientHostname, Channel serverChannel, String serverListenId) {
        String host;
        int port;
        try {
            String[] split = clientHostname.split(":");
            host = split[0];
            port = Integer.parseInt(split[1]);
        } catch (Exception e) {
            logger.error("解析失败clientHostname信息失败", e);
            return false;
        }
        intranetBootstrap.connect(host, port).addListener((ChannelFutureListener) (future) -> {
            if (future.isSuccess()) {
                if (logger.isInfoEnabled()) {
                    logger.info("向内部[{}:{}]建立连接成功", host, port);
                }
                Channel intranetChannel = future.channel();
                intranetChannel.config().setAutoRead(false);
                serverChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_CHANNEL).set(intranetChannel);
                serverChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_HOST).set(host);
                serverChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_PORT).set(port);
                serverChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(serverListenId);
                intranetChannel.attr(AttributeKeyConstants.CLIENT_SERVER_CHANNEL).set(serverChannel);
                intranetChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(serverListenId);
                serverChannel.writeAndFlush(BinaryMessage.buildConnectMessage(serverListenId, "clientId".getBytes(StandardCharsets.UTF_8)));
                // 保证读消息的时候已经设置好了attr
                intranetChannel.config().setAutoRead(true);
                GoThroughContext.addIntranetChannel(serverListenId, intranetChannel);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("向内部[{}:{}]建立连接失败", host, port);
                }
                serverChannel.writeAndFlush(BinaryMessage.buildDisconnectMessage(serverListenId));
            }
        });
        return true;
    }

    public static boolean reconnect(Bootstrap intranetBootstrap, Channel serverChannel) {
        String host = serverChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_HOST).get();
        Integer port = serverChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_PORT).get();
        String serverListenId = serverChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).get();
        if (StringUtil.isNullOrEmpty(host) || Objects.isNull(port)) {
            if (logger.isDebugEnabled()) {
                logger.debug("重新连接失败,参数不存在");
            }
            return false;
        }
        try {
            ChannelFuture channelFuture = intranetBootstrap.connect(host, port).sync().await();
            if (channelFuture.isSuccess()) {
                if (logger.isInfoEnabled()) {
                    logger.info("向内部[{}:{}]重新建立连接成功", host, port);
                }
                Channel intranetChannel = channelFuture.channel();
                intranetChannel.config().setAutoRead(false);
                serverChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_CHANNEL).set(intranetChannel);
                intranetChannel.attr(AttributeKeyConstants.CLIENT_SERVER_CHANNEL).set(serverChannel);
                intranetChannel.attr(AttributeKeyConstants.SERVER_LISTEN_ID).set(serverListenId);
                // 保证读消息的时候已经设置好了attr
                intranetChannel.config().setAutoRead(true);
                GoThroughContext.addIntranetChannel(serverListenId, intranetChannel);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("向内部[{}:{}]重新建立连接成功", host, port);
                }
                serverChannel.writeAndFlush(BinaryMessage.buildDisconnectMessage(serverListenId));
            }
            return true;
        } catch (InterruptedException e) {
            logger.error("重新连接被打断", e);
            return false;
        }
    }
}
