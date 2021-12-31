package com.future94.gothrough.protocol.nio.handler.codec;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;

/**
 * {@link com.future94.gothrough.protocol.nio.server.IServer}的解码器
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
}
