package com.future94.gothrough.protocol.nio.handler.codec;

/**
 * 将要写入{@link java.nio.channels.SocketChannel}的数据进行{@link byte[]}编码
 * @author weilai
 */
public abstract class MessageToByteEncoder<T> implements Encoder {

    @Override
    public byte[] encode(Object msg) throws Exception {
        @SuppressWarnings("unchecked")
        T imsg = (T) msg;
        return encoder(imsg);
    }

    public abstract byte[] encoder(T msg) throws Exception;
}