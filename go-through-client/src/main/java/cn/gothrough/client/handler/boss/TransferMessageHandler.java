package cn.gothrough.client.handler.boss;

import cn.gothrough.client.utils.IntranetUtils;
import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理Listen服务端发送过来的{@link BinaryMessage#TYPE_TRANSFER}消息
 *
 * @author weilai
 */
public class TransferMessageHandler implements MessageHandler {

    private static Logger logger = LoggerFactory.getLogger(TransferMessageHandler.class);

    private final Bootstrap intranetBootstrap;

    public TransferMessageHandler(Bootstrap intranetBootstrap) {
        this.intranetBootstrap = intranetBootstrap;
    }

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_TRANSFER == type;
    }

    /**
     * 将收到的服务端数据转发代理Channel
     */
    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        Channel serverChannel = ctx.channel();
        Channel clientIntranetChannel = serverChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_CHANNEL).get();
        boolean reconnect = false;
        if (clientIntranetChannel != null) {
            if (!clientIntranetChannel.isActive()) {
                clientIntranetChannel.closeFuture().addListener(ChannelFutureListener.CLOSE);
                reconnect = true;
            }
        } else {
            reconnect = true;
        }
        if (reconnect && !IntranetUtils.reconnect(intranetBootstrap, serverChannel)) {
            return false;
        }
        clientIntranetChannel = serverChannel.attr(AttributeKeyConstants.CLIENT_INTRANET_CHANNEL).get();
        ByteBuf buffer = ctx.alloc().buffer(message.getByteBuffer().length);
        buffer.writeBytes(message.getByteBuffer());
        clientIntranetChannel.writeAndFlush(buffer);
        return true;
    }
}
