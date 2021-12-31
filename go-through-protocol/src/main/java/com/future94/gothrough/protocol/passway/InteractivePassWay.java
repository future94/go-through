package com.future94.gothrough.protocol.passway;

import com.future94.gothrough.common.utils.ByteBufferUtils;
import com.future94.gothrough.protocol.thread.AbstractNIORunnable;
import com.future94.gothrough.protocol.thread.GoThroughNioContainer;
import com.future94.gothrough.protocol.thread.GoThroughThreadFactory;
import com.future94.gothrough.protocol.thread.ThreadManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
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
public class InteractivePassWay extends AbstractNIORunnable implements Runnable {

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
    private Socket recvSocket;
    @Setter
    private Socket sendSocket;

    private OutputStream outputStream;

    private SocketChannel outputChannel;

    private InteractivePassWay() {

    }

    public InteractivePassWay(ThreadManager threadManager, Socket recvSocket, Socket sendSocket) {
        this(threadManager, recvSocket, sendSocket, 1 << 12);
    }

    public InteractivePassWay(ThreadManager threadManager, Socket recvSocket, Socket sendSocket, int streamCacheSize) {
        this.threadManager = threadManager;
        this.recvSocket = recvSocket;
        this.sendSocket = sendSocket;
        this.streamCacheSize = streamCacheSize;
    }

    private final ExecutorService executorService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), GoThroughThreadFactory.create("interactive-pass-way"), new ThreadPoolExecutor.CallerRunsPolicy());

    private synchronized OutputStream getOutputStream() throws IOException {
        if (Objects.isNull(this.outputStream)) {
            this.outputStream = this.sendSocket.getOutputStream();
        }
        return this.outputStream;
    }

    private synchronized SocketChannel getOutputChannel() {
        if (Objects.isNull(this.outputChannel)) {
            this.outputChannel = this.sendSocket.getChannel();
        }
        return this.outputChannel;
    }

    /**
     * 向输出通道输出数据
     * <p>
     * 这里不只是为了DMA而去用DMA，而是这里有奇葩问题
     * <p>
     * 如能采用了SocketChannel，而去用outputStream的时候，不管输入输出，都会有奇怪的问题，比如输出会莫名的阻塞住
     * <p>
     * 整体就是如果能用nio的方法，但是用了bio形式都会各种什么 NullPointException、IllageSateException 等等错误
     * <p>
     */
    private void write(ByteBuffer byteBuffer) throws IOException {
        SocketChannel outputChannel;
        OutputStream outputStream;
        if (Objects.nonNull((outputChannel = this.getOutputChannel()))) {
            ByteBufferUtils.channelWrite(outputChannel, byteBuffer);
        } else {
            outputStream = this.getOutputStream();
            outputStream.write(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
            outputStream.flush();
        }
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = this.recvSocket.getInputStream();
            int len;
            byte[] arrayTemp = new byte[this.streamCacheSize];
            while (this.alive && (len = inputStream.read(arrayTemp)) > 0) {
                this.write(ByteBuffer.wrap(arrayTemp, 0, len));
            }
        } catch (IOException e) {
            // do nothing
        }

        log.debug("one InputToOutputThread closed");

        // 传输完成后退出
        this.cancel();
    }

    // ============== nio =================

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private ByteBuffer byteBuffer;

    private ByteBuffer obtainByteBuffer() {
        ByteBuffer byteBuffer = this.byteBuffer;
        if (Objects.isNull(byteBuffer)) {
            if (Objects.isNull(this.getOutputChannel())) {
                byteBuffer = ByteBuffer.allocate(this.streamCacheSize);
            } else {
                // 输入输出可以使用channel，此处则使用DirectByteBuffer，这时候才真正体现出了DMA
                byteBuffer = ByteBuffer.allocateDirect(this.streamCacheSize);
            }
            this.byteBuffer = byteBuffer;
        }
        return byteBuffer;
    }

    @Override
    public void doProcess(SelectionKey key) {
        if (this.alive) {
            ByteBuffer buffer = this.obtainByteBuffer();
            SocketChannel inputChannel = (SocketChannel) key.channel();
            try {
                int len;
                do {
                    buffer.clear();
                    len = inputChannel.read(buffer);
                    if (len > 0) {
                        buffer.flip();
                        if (buffer.hasRemaining()) {
                            this.write(buffer);
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

    /**
     * 判断是否可用
     */
    public boolean isValid() {
        return this.alive;
    }

    /**
     * 退出
     */
    public void cancel() {
        if (!this.alive) {
            return;
        }
        this.alive = false;

        if (!threadManager.getNio()) {
            GoThroughNioContainer.release(this.recvSocket.getChannel());
        }


        try {
            Socket sendSocket;
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
        SocketChannel recvChannel = this.recvSocket.getChannel();
        if (threadManager.getNio() || Objects.isNull(recvChannel)) {
            executorService.execute(this);
        } else {
            try {
                GoThroughNioContainer.register(recvChannel, SelectionKey.OP_READ, this);
            } catch (IOException e) {
                log.error("nio register error", e);
                this.cancel();
            }
        }
    }
}
