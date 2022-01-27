package com.future94.gothrough.protocol.nio.thread.client.thread;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;
import com.future94.gothrough.protocol.nio.thread.AbstractSelectThread;
import com.future94.gothrough.protocol.nio.thread.client.NioClient;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 处理SocketChannel读写线程
 * @author weilai
 */
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class ClientSelectThread extends AbstractSelectThread {

    /**
     * NIO客户端
     */
    private NioClient client;

    /**
     * 连接Server的SocketChannel
     */
    private final SocketChannel socketChannel;

    /**
     * 客户端操作的buffer
     */
    private final FrameBuffer frameBuffer;

    public ClientSelectThread(NioClient client) throws IOException {
        this("Selector-Thread-Listen-" + client.getPort(), client);
    }

    public ClientSelectThread(String threadName, NioClient client) throws IOException {
        super(client.getChannelReadableHandlers(), client.getEncoder(), client.getDecoder());
        super.setName(threadName);
        this.client = client;
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(client.getIp(), client.getPort()));
        socketChannel.configureBlocking(false);
        this.socketChannel = socketChannel;
        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        FrameBuffer frameBuffer = new FrameBuffer(this, selectionKey);
        selectionKey.attach(frameBuffer);
        this.frameBuffer = frameBuffer;
        client.setConnected();
    }

    @Override
    public void run() {
        for (; client.isConnected() && client.isStarted() && !checkInterrupted(); ) {
            select();
            processInterestChanges();
        }
    }

    private boolean checkInterrupted() {
        boolean interrupted = isInterrupted();
        if (interrupted) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                log.warn("close socket channel error", e);
            }
        }
        return interrupted;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    /**
     * 向SocketChanel写入数据
     */
    public boolean write(Object msg) {
        FrameBuffer buffer = getBuffer(socketChannel);
        if (buffer == null) {
            log.warn("Failed to write object data [{}] to the socket channel [{}]", msg.toString(), socketChannel.toString());
            return false;
        }
        return buffer.write(msg);
    }

    @Override
    public FrameBuffer getBuffer(SocketChannel socketChannel) {
        return this.socketChannel.equals(socketChannel) ? this.frameBuffer : null;
    }
}
