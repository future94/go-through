package com.future94.gothrough.protocol.nio.buffer;

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
     * <p>这个状态说明{@link java.nio.channels.SocketChannel}中的数据到{@link java.nio.ByteBuffer}中，接下来会调用{@link com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler}, 然后调用{@link FrameBuffer#invoke()}.
     */
    READ_FRAME_COMPLETE,

    /**
     * 等待写
     *
     * <p>这个是一个中间态，调用{@link FrameBuffer#invoke()}成功将{@link com.future94.gothrough.protocol.nio.handler.ChannelWritableHandler}的数据写入{@link java.nio.ByteBuffer}后为这个状态，但是随后就会被更新为{@link #WRITING_FRAME}.
     */
    WAITING_WRITE_FRAME,

    /**
     * 写入中
     *
     * <p>这个状态说明{@link com.future94.gothrough.protocol.nio.handler.ChannelWritableHandler}的数据已经写入到了{@link java.nio.ByteBuffer}，等待{@link java.nio.channels.SelectionKey#OP_WRITE}事件将{@link java.nio.ByteBuffer}写入{@link java.nio.channels.SocketChannel}
     */
    WRITING_FRAME,

    /**
     * 写入完成
     *
     * <p>这个是一个中间态，调用{@link FrameBuffer#write()}将{@link java.nio.ByteBuffer}完全写入{@link java.nio.channels.SocketChannel}之后为这个状态，但是随后就会被更新为{@link #PREPARE_READ_FRAME}.
     */
    WRITE_FRAME_COMPLETE,

    /**
     * 关闭
     *
     * <p>调用{@link FrameBuffer#invoke()}失败设置为这个状态.
     */
    FRAME_COLSE,
}
