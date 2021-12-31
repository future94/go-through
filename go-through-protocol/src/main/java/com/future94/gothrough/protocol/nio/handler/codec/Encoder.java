package com.future94.gothrough.protocol.nio.handler.codec;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;

/**
 * {@link com.future94.gothrough.protocol.nio.server.IServer}的编码器
 * 配合{@link Decoder}进行使用
 *
 * @author weilai
 */
public interface Encoder{

    /**
     * 对数据进行编码
     * @param msg           要编码的数据
     * @param buffer        要写入的buffer
     * @return {@code true}
     * @throws Exception    编码失败
     */
    boolean encode(Object msg, FrameBuffer buffer) throws Exception;

    /**
     * 对数据进行编码
     * @param msg           要编码的数据
     * @return {@code true}
     * @throws Exception    编码失败
     */
    byte[] encode(Object msg) throws Exception;
}
