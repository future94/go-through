package com.future94.gothrough.protocol.nio.buffer;

import com.future94.gothrough.common.utils.ByteBufferUtils;
import com.future94.gothrough.protocol.nio.thread.AbstractSelectThread;
import com.future94.gothrough.protocol.nio.thread.server.thread.ServerSelectorThread;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author weilai
 */
@Slf4j
@EqualsAndHashCode(exclude = "selectorThread")
public class FrameBuffer {

    /**
     * 内部读取实际操作的缓冲区，初始化为4个字节
     */
    private ByteBuffer buffer;

    /**
     * 所属的{@link ServerSelectorThread}
     */
    private final AbstractSelectThread selectorThread;

    /**
     * 最大同时可读空间，默认为{@link Long#MAX_VALUE}，具体查看{@link #read()}方法.
     *
     * <p>如果单次传输byte[]数量大于maxReadBufferBytes，则无法读取
     *
     * <p>如果累计传输byte[]数量大于maxReadBufferBytes，则当次直接返回等待下次
     */
    private final Long maxReadBufferBytes;

    /**
     * 记录当前{@link #buffer}已经读取的字节数
     */
    private final LongAdder readBufferBytesAllocated = new LongAdder();

    /**
     * 选择键
     */
    private SelectionKey selectionKey;

    /**
     * 所属的SocketChannel
     */
    private final SocketChannel socketChannel;

    /**
     * 当写就绪时要写入的数据队列
     */
    private final BlockingQueue<byte[]> waitingWriteQueue = new LinkedBlockingQueue<>();

    /**
     * 当前{@link FrameBuffer}的读状态，初始化为准备读
     */
    private volatile FrameBufferStateEnum readState = FrameBufferStateEnum.PREPARE_READ_FRAME;

    /**
     * 当前{@link FrameBuffer}的写状态，初始化为等待写
     */
    private volatile FrameBufferStateEnum writeState = FrameBufferStateEnum.WAITING_WRITE_FRAME;

    public FrameBuffer(AbstractSelectThread selectorThread, SelectionKey selectionKey) {
        this(selectorThread, selectionKey, Long.MAX_VALUE);
    }

    public FrameBuffer(AbstractSelectThread selectorThread, SelectionKey selectionKey, long maxReadBufferBytes) {
        this.selectorThread = selectorThread;
        this.selectionKey = selectionKey;
        this.socketChannel = (SocketChannel) selectionKey.channel();
        this.maxReadBufferBytes = maxReadBufferBytes;
        this.initByteBuffer();
    }

    /**
     * 重置状态
     * 当状态{@link #readState}为{@link FrameBufferStateEnum#WRITE_FRAME_COMPLETE}为时，
     * 需要将设置{@link #readState}为{@link FrameBufferStateEnum#PREPARE_READ_FRAME}状态
     */
    private void setPrepareReadState() {
        selectionKey.interestOps(SelectionKey.OP_READ);
        this.readState = FrameBufferStateEnum.PREPARE_READ_FRAME;
        this.initByteBuffer();
    }

    /**
     * 初始化{@link #buffer}
     * 因为要先接收一个int大小的frameSize值，所以这里初始化为4个byte
     */
    private void initByteBuffer() {
        this.buffer = ByteBuffer.allocate(4);
    }

    /**
     * 读取SocketChannel数据到Buffer中
     * @return {@code true} 读取成功
     */
    public boolean read() {
        // 准备读
        if (readState == FrameBufferStateEnum.PREPARE_READ_FRAME) {
            if (!readSocketChannelBuffer()) {
                return false;
            }
            if (buffer.remaining() == 0) {
                int frameSize = buffer.getInt(0);
                if (frameSize <= 0) {
                    log.error("Read an invalid frame size of [{}].", frameSize);
                    return false;
                }
                // 如果单次传输byte[]数量大于maxReadBufferBytes，则无法读取
                if (frameSize > maxReadBufferBytes) {
                    log.error("Read a frame size of [{}], which is bigger than the maximum allowable buffer size for maxReadBufferBytes.", frameSize);
                    return false;
                }
                // 如果累计传输byte[]数量大于maxReadBufferBytes，则当次直接返回等待下次
                if (readBufferBytesAllocated.longValue() + frameSize > maxReadBufferBytes) {
                    return true;
                }
                readBufferBytesAllocated.add(frameSize);
                buffer = ByteBuffer.allocate(frameSize);
                // 设置为读取中
                readState = FrameBufferStateEnum.READING_FRAME;
            } else {
                return true;
            }
        }
        // 读取中
        if (readState == FrameBufferStateEnum.READING_FRAME) {
            if (!readSocketChannelBuffer()) {
                return false;
            }

            if (buffer.remaining() == 0) {
                selectionKey.interestOps(0);
                // 设置为读取完成
                readState = FrameBufferStateEnum.READ_FRAME_COMPLETE;
            }
            return true;
        }
        log.error("Read was called but state is invalid (" + readState + ")");
        return false;
    }

    /**
     * 读取channel中的数据
     */
    private boolean readSocketChannelBuffer() {
        try {
            return socketChannel.read(buffer) >= 0;
        } catch (IOException e) {
            log.error("Got an IOException in internalRead!", e);
            return false;
        }
    }

    /**
     * buffer是否读取完成
     *
     * @return {@code true} 读取完成
     */
    public boolean isReadCompleted() {
        return readState == FrameBufferStateEnum.READ_FRAME_COMPLETE;
    }

    /**
     * 将数据写入buffer
     * @param msg           要写入的数据
     *                      如果msg是byte[]类型不会进行编码
     * @return {@code true} 写入buffer成功
     */
    public boolean write(Object msg) {
        try {
            if (msg instanceof byte[]) {
                waitingWriteQueue.put((byte[]) msg);
            } else {
                waitingWriteQueue.put(selectorThread.getEncoder().encode(msg));
            }
            if (writeState == FrameBufferStateEnum.WRITE_FRAME_COMPLETE || writeState == FrameBufferStateEnum.WAITING_WRITE_FRAME) {
                writeState = FrameBufferStateEnum.WAITING_WRITE_FRAME;
                // 注册SelectionKey.OP_WRITE事件
                // 设置state为FrameBufferStateEnum.WRITING_FRAME
                processSelectInterestChange(true);
            }
            return true;
        } catch (ClassCastException ex) {
            log.error("Got an Exception while encode() in selector thread [{}]!", this.selectorThread.getName(), ex);
        } catch (Throwable t) {
            log.error("Unexpected throwable while invoking!", t);
        }
        readState = FrameBufferStateEnum.FRAME_COLSE;
        // 调用close()
        // 调用selectionKey.cancel();
        processSelectInterestChange(true);
        return false;
    }

    /**
     * 像缓冲区写入数据
     * @return 是否写入成功
     */
    public boolean write() {
        if (this.writeState == FrameBufferStateEnum.WRITING_FRAME) {
            try {
                readBufferBytesAllocated.add(-buffer.array().length);
                for (; this.selectorThread.isAlive(); ) {
                    byte[] writePayload = waitingWriteQueue.poll();
                    if (writePayload == null || writePayload.length == 0) {
                        break;
                    }
                    ByteBufferUtils.channelWrite(socketChannel, writePayload);
                }
            } catch (IOException e) {
                log.warn("Got an IOException during write!", e);
                return false;
            }
            if (waitingWriteQueue.isEmpty()) {
                this.writeState = FrameBufferStateEnum.WRITE_FRAME_COMPLETE;
                processSelectInterestChange(true);
            }
            return true;
        }
        log.error("Write was called, but state is invalid [{}]", this.writeState.name());
        return false;
    }

    /**
     * 清除
     */
    public void close() {
        if (readState == FrameBufferStateEnum.READING_FRAME || readState == FrameBufferStateEnum.READ_FRAME_COMPLETE) {
            readBufferBytesAllocated.add(-buffer.array().length);
        }
    }

    /**
     * 获取buffer中的数据
     *
     * @return {@code byte[]}
     */
    public byte[] readBufferBytes() {
        return buffer.array();
    }

    /**
     * 处理SelectInterest更改
     */
    public void processSelectInterestChange(boolean write) {
        if (Thread.currentThread() == this.selectorThread) {
            changeSelectInterests(write);
        } else {
            if (write) {
                this.selectorThread.processSelectInterestWriteChange(this);
            } else {
                this.selectorThread.processSelectInterestReadChange(this);
            }
        }
    }

    /**
     * 更改SelectInterest
     */
    public void changeSelectInterests(boolean write) {
        if (write) {
            if (writeState == FrameBufferStateEnum.WAITING_WRITE_FRAME) {
                // set the OP_WRITE interest
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                writeState = FrameBufferStateEnum.WRITING_FRAME;
            } else if (writeState == FrameBufferStateEnum.WRITE_FRAME_COMPLETE) {
                selectionKey.interestOps(SelectionKey.OP_READ);
                writeState = FrameBufferStateEnum.WAITING_WRITE_FRAME;
            }
        } else {
            if (readState == FrameBufferStateEnum.READ_FRAME_COMPLETE) {
                setPrepareReadState();
            }
        }
        if (readState == FrameBufferStateEnum.FRAME_COLSE || writeState == FrameBufferStateEnum.FRAME_COLSE) {
            this.close();
            selectionKey.cancel();
        }
    }
}
