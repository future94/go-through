package com.future94.gothrough.protocol.nio.handler.codec;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;

/**
 * 将要写入{@link java.nio.channels.SocketChannel}的数据进行{@link byte[]}编码
 * @author weilai
 */
public abstract class MessageToByteEncoder<T> implements Encoder {

    @Override
    public boolean encode(Object msg, FrameBuffer buffer) throws Exception {
        @SuppressWarnings("unchecked")
        T imsg = (T) msg;
        return buffer.write(encode(imsg));
    }

}