package cn.gothrough.protocol.codec;

import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.charset.StandardCharsets;

/**
 * 二进制消息{@link BinaryMessage}解码器
 * @author weilai
 */
public class BinaryMessageDecoder extends LengthFieldBasedFrameDecoder {

    public BinaryMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected BinaryMessage decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf buffer = (ByteBuf) super.decode(ctx, in);
        if (buffer == null) {
            return null;
        }
        if (buffer.readableBytes() < BinaryMessage.HEAD_SIZE) {
            return null;
        }
        int frameLength = buffer.readInt();
        if (buffer.readableBytes() < frameLength) {
            return null;
        }
        BinaryMessage message = new BinaryMessage();
        message.setType(buffer.readByte());
        message.setSequence(buffer.readLong());
        final int dataLength = buffer.readInt();
        byte[] bytes = new byte[dataLength];
        buffer.readBytes(bytes);
        message.setData(new String(bytes, StandardCharsets.UTF_8));

        byte[] bufferByte = new byte[frameLength - BinaryMessage.TYPE_SIZE - BinaryMessage.SEQUENCE_SIZE - BinaryMessage.DATA_SIZE - dataLength];
        buffer.readBytes(bufferByte);
        message.setByteBuffer(bufferByte);
        buffer.release();
        return message;
    }
}
