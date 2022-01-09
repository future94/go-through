package com.future94.gothrough.protocol.nio.server;

import com.future94.gothrough.protocol.nio.handler.AcceptHandler;
import com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.ChannelWritableHandler;

import java.nio.channels.SocketChannel;

/**
 * NIO 实现的服务端
 * @author weilai
 */
public interface NioServer extends IServer{

    /**
     * 设置{@link com.future94.gothrough.protocol.nio.thread.SelectorThread}线程数
     * @param selectorThreadCount 线程数
     */
    void setSelectorThreadCount(int selectorThreadCount);

    /**
     * 设置当{@link java.nio.channels.SelectionKey#OP_ACCEPT}事件的回调
     * @param acceptHandler 要回调的处理逻辑
     */
    void setAcceptHandler(AcceptHandler acceptHandler);

    /**
     * 设置当{@link java.nio.channels.SelectionKey#OP_READ}事件的回调
     * @param channelReadableHandler 要回调的处理逻辑
     */
    void setReadableHandler(ChannelReadableHandler channelReadableHandler);

    /**
     * 设置当{@link java.nio.channels.SelectionKey#OP_WRITE}事件的回调
     * @param channelWritableHandler 要回调的处理逻辑
     */
    void setWritableHandler(ChannelWritableHandler channelWritableHandler);

    /**
     * 向{@link SocketChannel}中写入数据
     * @param socketChannel 要写入的socketChannel
     * @param msg           要写入的数据，通过{@link GoThroughNioServer#getEncoder()}}编码
     * @throws Exception    写入异常
     */
    void writeChannel(SocketChannel socketChannel, Object msg) throws Exception;

    /**
     * 读取{@link SocketChannel}中的数据，这个SocketChannel必须是阻塞的
     * @param socketChannel 要读取的socketChannel
     * @return Object       通过{@link GoThroughNioServer#getDecoder()}解码后的数据
     * @throws Exception    读取异常
     */
    Object readChannel(SocketChannel socketChannel) throws Exception;
}
