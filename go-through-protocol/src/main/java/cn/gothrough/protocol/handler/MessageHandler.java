package cn.gothrough.protocol.handler;

import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author weilai
 */
public interface MessageHandler {

    boolean supports(byte type);

    boolean process(ChannelHandlerContext ctx, BinaryMessage message);
}
