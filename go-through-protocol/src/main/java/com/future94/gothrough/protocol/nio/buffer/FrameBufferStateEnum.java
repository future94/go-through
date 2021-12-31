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
     * <p>这个是分配好读取buffer之后的状态，如果是这个状态，读取socketChannel中的数据到buffer中即可.
     *
     * <p>读取完成后会将状态设置为{@link #READ_FRAME_COMPLETE}.
     */
    READING_FRAME,

    /**
     * 读取完成
     */
    READ_FRAME_COMPLETE,

    /**
     * 等待写
     */
    WAITING_WRITE_FRAME,

    /**
     * 写入中
     */
    WRITING_FRAME,

    /**
     * 写入完成
     */
    WRITE_FRAME_COMPLETE,

    /**
     * 关闭
     */
    FRAME_COLSE,
}
