package com.future94.gothrough.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author weilai
 */
@Slf4j
public class ByteBufferUtils {

    public static boolean channelWrite(SocketChannel socketChannel, byte[] writePayload) throws IOException {
        ByteBuffer writeBuffer = ByteBuffer.wrap(writePayload, 0, writePayload.length);
        if (ByteBufferUtils.channelWrite(socketChannel, ByteBuffer.wrap(ByteBufferUtils.intToBytes(writeBuffer.array().length))) < 0) {
            log.warn("write socket channel length fail, payload [{}]", writePayload);
            return false;
        }
        if (ByteBufferUtils.channelWrite(socketChannel, writeBuffer) < 0) {
            log.warn("write socket channel data fail, payload [{}]", writePayload);
            return false;
        }
        return true;
    }

    public static int channelWrite(WritableByteChannel channel, ByteBuffer buffer) throws IOException {
        int sum = 0;
        while (buffer.hasRemaining()) {
            sum += channel.write(buffer);
        }
        return sum;
    }

    /**
     * 将byte[]转换为int
     * @param byteArr 要转化的byte[]
     * @return {@code int}
     */
    public static int bytesToInt(byte[] byteArr) {
        int count = 0;
        for (int i = 0; i < 4; ++i) {
            count <<= 8;
            count |= byteArr[i] & 0xFF;
        }
        return count;
    }

    /**
     * 将int转换为byte[]
     * @param source 要转化的int
     * @return {@code byte[]}
     */
    public static byte[] intToBytes(int source) {
        return new byte[]{(byte) ((source >> 24) & 0xFF), (byte) ((source >> 16) & 0xFF),
                (byte) ((source >> 8) & 0xFF), (byte) (source & 0xFF)};
    }
}
