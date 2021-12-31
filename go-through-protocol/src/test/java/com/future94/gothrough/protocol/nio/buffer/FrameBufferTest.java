package com.future94.gothrough.protocol.nio.buffer;

import java.nio.ByteBuffer;

/**
 * @author weilai
 */
class FrameBufferTest {

    public static void main(String[] args) {
        ByteBuffer buff = ByteBuffer.allocate(1024);
        String str = "helloWorld";
        buff.put(str.getBytes());
        buff.flip();
        System.out.println();
    }
}