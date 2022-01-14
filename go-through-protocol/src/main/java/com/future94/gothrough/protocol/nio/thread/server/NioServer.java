package com.future94.gothrough.protocol.nio.thread.server;

import com.future94.gothrough.protocol.nio.handler.AcceptHandler;
import com.future94.gothrough.protocol.nio.thread.server.thread.ServerSelectorThread;

import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * NIO 实现的服务端
 * @author weilai
 */
public interface NioServer extends IServer{

    /**
     * 设置{@link ServerSelectorThread}线程数
     * @param selectorThreadCount 线程数
     */
    void setSelectorThreadCount(int selectorThreadCount);

    /**
     * 设置当{@link java.nio.channels.SelectionKey#OP_ACCEPT}事件的回调
     * @param acceptHandler 要回调的处理逻辑
     */
    void setAcceptHandler(AcceptHandler acceptHandler);

    List<AcceptHandler> getAcceptHandlers();

    /**
     * 向{@link SocketChannel}中写入数据
     * @param socketChannel 要写入的socketChannel
     * @param msg           要写入的数据，通过{@link #getEncoder()}}编码
     * @throws Exception    写入异常
     */
    void writeChannel(SocketChannel socketChannel, Object msg) throws Exception;

    ServerSelectorThread chooseSelectorThread();
}
