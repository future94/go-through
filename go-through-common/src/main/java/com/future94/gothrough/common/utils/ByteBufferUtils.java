package com.future94.gothrough.common.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * @author weilai
 */
public class ByteBufferUtils {

    public static int channelWrite(WritableByteChannel channel, ByteBuffer buffer) throws IOException {
        int sum = 0;
        while (buffer.hasRemaining()) {
            sum += channel.write(buffer);
        }
        return sum;
    }

    public static int bytesToInt(byte[] byteArr) {
        int count = 0;
        for (int i = 0; i < 4; ++i) {
            count <<= 8;
            count |= byteArr[i] & 255;
        }
        return count;
    }

    public static byte[] intToBytes(int source) {
        return new byte[]{(byte) ((source >> 24) & 0xFF), (byte) ((source >> 16) & 0xFF),
                (byte) ((source >> 8) & 0xFF), (byte) (source & 0xFF)};
    }
}
