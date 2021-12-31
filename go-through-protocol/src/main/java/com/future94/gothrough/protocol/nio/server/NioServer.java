package com.future94.gothrough.protocol.nio.server;

import com.future94.gothrough.protocol.nio.handler.AcceptHandler;
import com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.ChannelWritableHandler;

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
     * 设置要写入的值
     * @param payload 具体的值
     */
    void setWriteData(String payload);

    /**
     * 设置要写入的值
     * @param payload 具体的值
     */
    void setWriteData(byte[] payload);

}
