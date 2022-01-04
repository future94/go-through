package com.future94.gothrough.protocol.nio.thread;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;
import com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler;
import com.future94.gothrough.protocol.nio.server.GoThroughNioServer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author weilai
 */
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class SelectorThread extends Thread {

    /**
     * Accept的选择
     */
    private final Selector selector;

    @Getter
    private final GoThroughNioServer serverManager;

    private final BlockingQueue<SocketChannel> queue = new LinkedBlockingDeque<>();

    protected final Set<FrameBuffer> selectInterestChanges = new HashSet<>();

    public SelectorThread(GoThroughNioServer serverManager) throws IOException {
        this("Selector-Thread-Listen-" + serverManager.getPort(), serverManager);
    }

    public SelectorThread(String threadName, GoThroughNioServer serverManager) throws IOException {
        super.setName(threadName);
        this.selector = SelectorProvider.provider().openSelector();
        this.serverManager = serverManager;
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
            for (; this.serverManager.isStart(); ) {
                select();
                processAcceptedConnections();
            }
        } catch (Throwable e) {
            log.error("The thread [{}] processing select method throws IOException ", getName(), e);
        } finally {
            this.serverManager.stop();
        }
    }

    private void processAcceptedConnections() {
        // Register accepted connections
        for (; this.serverManager.isStart(); ) {
            SocketChannel socketChannel = queue.poll();
            if (socketChannel == null) {
                break;
            }
            registerAccepted(socketChannel);
        }
    }

    /**
     * 处理
     */
    public void processSelectInterestChange(FrameBuffer frameBuffer) {
        synchronized (selectInterestChanges) {
            selectInterestChanges.add(frameBuffer);
        }
        selector.wakeup();
    }

    @SuppressWarnings("all")
    private void registerAccepted(SocketChannel socketChannel) {
        SelectionKey clientSelectionKey = null;
        try {
            clientSelectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
            FrameBuffer frameBuffer = new FrameBuffer(this, clientSelectionKey);
            clientSelectionKey.attach(frameBuffer);
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
     * 处理Selector事件
     */
    private void select() {
        try {
            int select = selector.select();
            if (select <= 0) {
                return;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (!selectionKey.isValid()) {
                    continue;
                }
                if (selectionKey.isReadable()) {
                    handleRead(selectionKey);
                } else if (selectionKey.isWritable()) {
                    handleWrite(selectionKey);
                } else {
                    // 在SelectorThread中select方法出现意外状态
                    log.warn("An unexpected state [{}] occurred in the select method on Selector Thread", selectionKey.interestOps());
                }
            }
        } catch (IOException e) {
            log.error("Got an IOException while selecting in selector thread [{}]!", super.getName(), e);
        }
    }

    /**
     * 读事件处理
     */
    private void handleRead(SelectionKey selectionKey) {
        FrameBuffer buffer = (FrameBuffer) selectionKey.attachment();
        if (!buffer.read()) {
            cleanupSelectionKey(selectionKey);
            return;
        }
        if (buffer.isReadCompleted()) {
            if (!doReadableHandler(buffer)) {
                cleanupSelectionKey(selectionKey);
            }
            if (!invokeWritable(selectionKey)) {
                cleanupSelectionKey(selectionKey);
            }
        }
    }

    /**
     * 写事件处理
     */
    private void handleWrite(SelectionKey selectionKey) {
        FrameBuffer buffer = (FrameBuffer) selectionKey.attachment();
        if (!buffer.write()) {
            cleanupSelectionKey(selectionKey);
        }
    }

    /**
     * 回调{@link com.future94.gothrough.protocol.nio.handler.ChannelWritableHandler}业务代码写入buffer
     */
    private boolean invokeWritable(SelectionKey selectionKey) {
        FrameBuffer buffer = (FrameBuffer) selectionKey.attachment();
        if (!buffer.invoke()) {
            cleanupSelectionKey(selectionKey);
            return false;
        }
        return true;
    }

    /**
     * 回调{@link ChannelReadableHandler}处理器
     *
     * @param buffer 已经读取好数据的buffer
     * @return {@code true} 回调成功
     */
    private boolean doReadableHandler(FrameBuffer buffer) {
        try {
            for (ChannelReadableHandler channelReadableHandler : serverManager.getChannelReadableHandlers()) {
                try {
                    channelReadableHandler.channelRead(serverManager.getDecoder().decode(buffer));
                } catch (ClassCastException e) {
                    log.warn("Got an ClassCastException while channelRead() in selector thread [{}]!", super.getName(), e);
                } catch (Exception e) {
                    log.error("Got an Exception while channelRead() in selector thread [{}]!", super.getName(), e);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Got an Exception while doReadableHandler() in selector thread [{}]!", super.getName(), e);
            return false;
        }
    }

    /**
     * 清除掉操作操作失败的SelectionKey
     *
     * @param selectionKey 要清除的selectionKey
     */
    private void cleanupSelectionKey(SelectionKey selectionKey) {
        FrameBuffer buffer = (FrameBuffer) selectionKey.attachment();
        if (buffer != null) {
            buffer.close();
        }
        selectionKey.cancel();
    }

}
