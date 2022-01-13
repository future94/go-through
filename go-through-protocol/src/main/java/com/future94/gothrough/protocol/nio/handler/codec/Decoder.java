package com.future94.gothrough.protocol.nio.handler.codec;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;

/**
 * {@link com.future94.gothrough.protocol.nio.thread.server.IServer}的解码器
 * 配合{@link Encoder}进行使用
 *
 * @author weilai
 */
public interface Decoder<T> {

    /**
     * 解码
     * @param buffer        要解码的buffer
     * @return T            实际解码出来的数据
     * @throws Exception    解码失败
     */
    T decode(FrameBuffer buffer) throws Exception;

    /**
     * 对应各种解码操作
     * @param payload       要解码的数据
     * @return T            实际解码的数据
     * @throws Exception    解码失败
     */
    T decode(byte[] payload) throws Exception;
}
