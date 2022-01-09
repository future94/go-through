package com.future94.gothrough.protocol.nio.handler.context;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;

import java.nio.channels.SocketChannel;

/**
 * @author weilai
 */
public class ChannelHandlerContext {

    /**
     * 已经读取好数据的buffer
     */
    private final FrameBuffer buffer;

    /**
     * Accept到的SocketChannel
     */
    private final SocketChannel socketChannel;

    public ChannelHandlerContext(FrameBuffer buffer, SocketChannel socketChannel) {
        this.buffer = buffer;
        this.socketChannel = socketChannel;
    }

    public boolean write(Object msg) {
        return buffer.writeBuffer(msg);
    }

    public SocketChannel getSocketChannel() {
        return this.socketChannel;
    }
}
