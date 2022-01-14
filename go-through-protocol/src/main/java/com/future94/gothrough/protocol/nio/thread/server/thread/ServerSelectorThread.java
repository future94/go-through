package com.future94.gothrough.protocol.nio.thread.server.thread;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;
import com.future94.gothrough.protocol.nio.handler.AcceptHandler;
import com.future94.gothrough.protocol.nio.thread.AbstractSelectThread;
import com.future94.gothrough.protocol.nio.thread.server.NioServer;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author weilai
 */
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class ServerSelectorThread extends AbstractSelectThread {

    private final NioServer server;

    private final BlockingQueue<SocketChannel> queue = new LinkedBlockingDeque<>();

    private final Map<SocketChannel, FrameBuffer> bufferCache = new ConcurrentHashMap<>();

    public ServerSelectorThread(NioServer server) throws IOException {
        this("Selector-Thread-Listen-" + server.getPort(), server);
    }

    public ServerSelectorThread(String threadName, NioServer server) throws IOException {
        super(server.getChannelReadableHandlers(), server.getEncoder(), server.getDecoder());
        super.setName(threadName);
        this.server = server;
    }

    /**
     * 将接收到的SocketChanel加入到队列
     *
     * @param socketChannel Accept事件接收到的SocketChannel
     */
    public void addQueue(SocketChannel socketChannel) {
        try {
            socketChannel.configureBlocking(false);
            queue.put(socketChannel);
            selector.wakeup();
        } catch (IOException | InterruptedException e) {
            log.error("Queued the SocketChannel received by the ACCEPT event as an exception.", e);
        }
    }

    @Override
    public void run() {
        try {
            for (; this.server.isStart(); ) {
                select();
                processAcceptedConnections();
                processInterestChanges();
            }
        } catch (Throwable e) {
            log.error("The thread [{}] processing select method throws IOException ", getName(), e);
        } finally {
            this.server.stop();
        }
    }

    private void processAcceptedConnections() {
        // Register accepted connections
        for (; this.server.isStart(); ) {
            final SocketChannel socketChannel = queue.poll();
            if (socketChannel == null) {
                break;
            }
            registerAccepted(socketChannel);
            executorService.execute( () -> doAcceptHandler(socketChannel));
        }
    }

    @SuppressWarnings("all")
    public void registerAccepted(SocketChannel socketChannel) {
        SelectionKey clientSelectionKey = null;
        try {
            clientSelectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
            FrameBuffer frameBuffer = new FrameBuffer(this, clientSelectionKey);
            clientSelectionKey.attach(frameBuffer);
            this.bufferCache.put(socketChannel, frameBuffer);
        } catch (IOException e) {
            log.warn("Failed to register accepted connection to selector!", e);
            cleanupSelectionKey(clientSelectionKey);
            try {
                socketChannel.close();
            } catch (IOException ex) {
                log.warn("Failed to close socketChannel", e);
            }
        }
    }

    /**
     * 回调{@link AcceptHandler}处理器
     */
    private void doAcceptHandler(SocketChannel socketChannel) {
        try {
            for (AcceptHandler acceptHandler : server.getAcceptHandlers()) {
                acceptHandler.accept(socketChannel);
            }
        } catch (Exception e) {
            log.error("Got an Exception while doAcceptHandler() in accept thread [{}]!", super.getName(), e);
        }
    }

    @Override
    public FrameBuffer getBuffer(SocketChannel socketChannel) {
        return this.bufferCache.get(socketChannel);
    }

    @Override
    public SelectionKey prepareWriteBuffer(SelectionKey selectionKey) throws ClosedChannelException {
        return selectionKey;
    }
}
