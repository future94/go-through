package cn.gothrough.protocol.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

/**
 * @author weilai
 */
class BinaryMessageTest {

    @Test
    public void encode() {
        BinaryMessage msg = new BinaryMessage();
        msg.setType((byte) 0x01);
        msg.setSequence(123L);
        msg.setData("hello");

        int bufferLength = BinaryMessage.FIXED_SIZE;
        final boolean hasData = msg.getData() != null;
        final boolean hasBuffer = msg.getByteBuffer() != null;
        if (hasData) {
            bufferLength += msg.getData().length();
        }
        if (hasBuffer) {
            bufferLength += msg.getByteBuffer().length;
        }
        ByteBuf out = Unpooled.buffer(bufferLength);
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
        System.out.println(ByteBufUtil.hexDump(out));
        //0x00 0x00 0x00 0x12 0x01 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x7b 0x00 0x00 0x00 0x05 0x68 0x65 0x6c 0x6c 0x6f
    }

}