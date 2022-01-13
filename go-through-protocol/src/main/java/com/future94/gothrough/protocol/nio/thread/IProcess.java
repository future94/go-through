package com.future94.gothrough.protocol.nio.thread;

import com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.codec.Decoder;
import com.future94.gothrough.protocol.nio.handler.codec.Encoder;

import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * @author weilai
 */
public interface IProcess {

    /**
     * 设置当{@link java.nio.channels.SelectionKey#OP_READ}事件的回调
     * @param channelReadableHandler 要回调的处理逻辑
     */
    void setReadableHandler(ChannelReadableHandler channelReadableHandler);

    /**
     * 获取当{@link java.nio.channels.SelectionKey#OP_READ}事件的处理器
     * @return {@code List}     事件处理器
     */
    List<ChannelReadableHandler> getChannelReadableHandlers();

    /**
     * 读取{@link SocketChannel}中的数据，这个SocketChannel必须是阻塞的
     * @param socketChannel 要读取的socketChannel
     * @return Object       通过{@link #getDecoder()}解码后的数据
     * @throws Exception    读取异常
     */
    Object readBlockSocketChannel(SocketChannel socketChannel) throws Exception;

    /**
     * 获取编码器
     * @return {@code Encoder}
     */
    Encoder getEncoder();

    /**
     * 设置编码器
     * @param encoder {@code Encoder}
     */
    void setEncoder(Encoder encoder);

    /**
     * 获取解码器
     * @return {@code Decoder}
     */
    Decoder<?> getDecoder();

    /**
     * 设置编码器
     * @param decoder {@code Decoder}
     */
    void setDecoder(Decoder<?> decoder);
}
