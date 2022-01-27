package com.future94.gothrough.protocol.nio.buffer;

import com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler;

/**
 * 缓冲区{@link FrameBuffer}的状态
 * @author weilai
 */
public enum FrameBufferStateEnum {

    /**
     * 准备读
     * <p>开始的时候会发送一个int大小的frameSize值, 读取到这个值后将{@code FrameBuffer#buffer}分配对应大小的内存, 然后将状态切换为{@link #READING_FRAME}.
     */
    PREPARE_READ_FRAME,

    /**
     * 读取中
     *
     * <p>这个是分配好读取{@link java.nio.ByteBuffer}大小之后的状态，如果是这个状态，等待{@link java.nio.channels.SelectionKey#OP_READ}事件准备就绪读取{@link java.nio.channels.SocketChannel}中的数据到{@link java.nio.ByteBuffer}中.
     *
     * <p>读取完成后会将状态设置为{@link #READ_FRAME_COMPLETE}.
     */
    READING_FRAME,

    /**
     * 读取完成
     *
     * <p>这个状态说明{@link java.nio.channels.SocketChannel}中的数据到{@link java.nio.ByteBuffer}中，接下来会调用{@link ChannelReadableHandler}, 然后被更新为{@link #PREPARE_READ_FRAME}.
     */
    READ_FRAME_COMPLETE,

    /**
     * 等待写
     *
     * <p>开始的时候为这个状态.
     */
    WAITING_WRITE_FRAME,

    /**
     * 写入中
     *
     * <p>这个状态说明已经调用{@link FrameBuffer#write(Object)}方法将数据已经写入到了{@code FrameBuffer#waitingWriteQueue}，
     * 并注册了{@link java.nio.channels.SelectionKey#OP_WRITE}事件，等事件就绪写入数据
     */
    WRITING_FRAME,

    /**
     * 写入完成
     *
     * <p>这个是一个中间态，表示已经将{@code FrameBuffer#waitingWriteQueue}完全写入{@link java.nio.channels.SocketChannel}之后为这个状态，但是随后就会被更新为{@link #WAITING_WRITE_FRAME}.
     */
    WRITE_FRAME_COMPLETE,

    /**
     * 关闭
     *
     * <p>调用{@link FrameBuffer#invoke()}失败设置为这个状态.
     */
    FRAME_COLSE,
}
