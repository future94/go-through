package com.future94.gothrough.protocol.passway;

import com.future94.gothrough.common.utils.ByteBufferUtils;
import com.future94.gothrough.protocol.thread.GoThroughThreadFactory;
import com.future94.gothrough.protocol.thread.ThreadManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author weilai
 */
@Slf4j
public class InteractivePassWay implements Runnable {

    private boolean alive = false;

    /**
     * 所属对象，完成后通知
     */
    @Setter
    private ThreadManager threadManager;

    /**
     * 缓存大小
     */
    @Setter
    private int streamCacheSize;

    @Setter
    private SocketChannel recvSocket;
    @Setter
    private SocketChannel sendSocket;

    private InteractivePassWay() {

    }

    public InteractivePassWay(ThreadManager threadManager, SocketChannel recvSocket, SocketChannel sendSocket) {
        this(threadManager, recvSocket, sendSocket, 1 << 12);
    }

    public InteractivePassWay(ThreadManager threadManager, SocketChannel recvSocket, SocketChannel sendSocket, int streamCacheSize) {
        this.threadManager = threadManager;
        this.recvSocket = recvSocket;
        this.sendSocket = sendSocket;
        this.streamCacheSize = streamCacheSize;
        this.byteBuffer = ByteBuffer.allocate(this.streamCacheSize);
    }

    private final ExecutorService executorService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), GoThroughThreadFactory.create("interactive-pass-way"), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void run() {
        if (this.alive) {
            ByteBuffer buffer = this.byteBuffer;
            SocketChannel inputChannel = this.recvSocket;
            try {
                int len;
                do {
                    buffer.clear();
                    len = inputChannel.read(buffer);
                    if (len > 0) {
                        buffer.flip();
                        if (buffer.hasRemaining()) {
                            ByteBufferUtils.channelWrite(this.sendSocket, buffer);
                        }
                    }
                } while (len > 0);
                // 如果不是负数，则还没有断开连接，返回继续等待
                if (len >= 0) {
                    return;
                }
            } catch (IOException e) {
                //
            }
        }

        log.debug("one InputToOutputThread closed");

        this.cancel();
    }

    // ============== nio =================

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private ByteBuffer byteBuffer;

    /**
     * 退出
     */
    public void cancel() {
        if (!this.alive) {
            return;
        }
        this.alive = false;

        try {
            SocketChannel sendSocket;
            if ((sendSocket = this.sendSocket) != null) {
                // TCP 挥手步骤，对方调用 shutdownOutput 后等价完成 socket.close
                sendSocket.shutdownOutput();
            }
        } catch (IOException e) {
            // do no thing
        }

        if (this.threadManager != null) {
            this.threadManager.noticeStopPassWay();
            this.threadManager = null;
        }
    }

    public void start() {
        if (this.alive) {
            return;
        }
        this.alive = true;
        SocketChannel recvChannel = this.recvSocket;
        if (Objects.isNull(recvChannel)) {
            executorService.execute(this);
        }
    }
}
