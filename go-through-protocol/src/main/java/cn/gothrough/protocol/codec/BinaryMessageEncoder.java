package cn.gothrough.protocol.codec;

import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 二进制消息{@link BinaryMessage}编码器
 * @author weilai
 */
public class BinaryMessageEncoder extends MessageToByteEncoder<BinaryMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, BinaryMessage msg, ByteBuf out) throws Exception {
        int bufferLength = BinaryMessage.FIXED_SIZE;
        final boolean hasData = msg.getData() != null;
        final boolean hasBuffer = msg.getByteBuffer() != null;
        if (hasData) {
            bufferLength += msg.getData().length();
        }
        if (hasBuffer) {
            bufferLength += msg.getByteBuffer().length;
        }
        out.writeInt(bufferLength);
        out.writeByte(msg.getType());
        out.writeLong(msg.getSequence());
        if (hasData) {
            out.writeInt(msg.getData().length());
            out.writeBytes(msg.getData().getBytes());
        } else {
            out.writeInt(0);
        }
        if (hasBuffer) {
            out.writeBytes(msg.getByteBuffer());
        }
    }

}
