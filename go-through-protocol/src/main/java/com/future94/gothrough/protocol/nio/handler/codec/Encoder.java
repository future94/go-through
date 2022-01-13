package com.future94.gothrough.protocol.nio.handler.codec;

/**
 * {@link com.future94.gothrough.protocol.nio.thread.server.IServer}的编码器
 * 配合{@link Decoder}进行使用
 *
 * @author weilai
 */
public interface Encoder{

    /**
     * 对数据进行编码
     * @param msg           要编码的数据
     * @return {@code true}
     * @throws Exception    编码失败
     */
    byte[] encode(Object msg) throws Exception;
}
