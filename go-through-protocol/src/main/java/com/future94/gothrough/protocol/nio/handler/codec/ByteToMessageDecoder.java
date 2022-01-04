package com.future94.gothrough.protocol.nio.handler.codec;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;

/**
 * 将要{@link java.nio.channels.SocketChannel}读取的数据进行{@link byte[]}解码
 * @author weilai
 */
public abstract class ByteToMessageDecoder<T> implements Decoder<T> {

    @Override
    public T decode(FrameBuffer buffer) throws Exception {
        byte[] bytes = buffer.readBufferBytes();
        return decode(bytes);
    }
}
