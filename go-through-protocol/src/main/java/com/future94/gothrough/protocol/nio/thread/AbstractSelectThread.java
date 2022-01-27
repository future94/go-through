package com.future94.gothrough.protocol.nio.thread;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;
import com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.codec.Decoder;
import com.future94.gothrough.protocol.nio.handler.codec.Encoder;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;
import com.future94.gothrough.protocol.thread.GoThroughThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author weilai
 */
@Slf4j
public abstract class AbstractSelectThread extends Thread {

    /**
     * 事件选择器
     */
    protected final Selector selector;

    /**
     * 有读状态修改的buffer集合
     */
    protected final Set<FrameBuffer> selectInterestReadChanges = new HashSet<>();

    /**
     * 有写状态修改的buffer集合
     */
    protected final Set<FrameBuffer> selectInterestWriteChanges = new HashSet<>();

    /**
     * 当{@link java.nio.channels.SelectionKey#OP_READ}事件的回调
     */
    private final List<ChannelReadableHandler> objectHandlers;

    /**
     * 读取数据后执行业务{@link ChannelReadableHandler}的线程池
     */
    protected final ExecutorService executorService = new ThreadPoolExecutor(1, 10, 60L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), GoThroughThreadFactory.create("business"));

    /**
     * 编码器
     */
    @Getter
    private final Encoder encoder;

    /**
     * 解码器
     */
    private final Decoder<?> decoder;

    public AbstractSelectThread(List<ChannelReadableHandler> objectHandlers, Encoder encoder, Decoder<?> decoder) throws IOException {
        this.objectHandlers = objectHandlers;
        this.encoder = encoder;
        this.decoder = decoder;
        this.selector = Selector.open();
    }

    /**
     * 处理Selector事件
     */
    protected void select() {
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
    private void handleRead(final SelectionKey selectionKey) {
        FrameBuffer buffer = (FrameBuffer) selectionKey.attachment();
        if (!buffer.read()) {
            cleanupSelectionKey(selectionKey);
            return;
        }
        if (buffer.isReadCompleted()) {
            try {
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                byte[] bytes = buffer.readBufferBytes();
                executorService.execute( () -> doReadableHandler(bytes, buffer, socketChannel));
            } catch (Exception e) {
                cleanupSelectionKey(selectionKey);
            }
        }
        buffer.processSelectInterestChange(false);
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
     * 向SocketChanel写入数据
     */
    public boolean write(SocketChannel socketChannel, Object msg) {
        FrameBuffer buffer = getBuffer(socketChannel);
        if (buffer == null) {
            log.warn("Failed to write object data [{}] to the socket channel [{}]", msg.toString(), socketChannel.toString());
            return false;
        }
        return buffer.write(msg);
    }

    /**
     * 回调{@link ChannelReadableHandler}处理器
     *
     * @return {@code true} 回调成功
     */
    private boolean doReadableHandler(byte[] decode, FrameBuffer buffer, SocketChannel socketChannel) {
        try {
            ChannelHandlerContext ctx = new ChannelHandlerContext(buffer, socketChannel);
            for (ChannelReadableHandler channelReadableHandler : objectHandlers) {
                if (!channelReadableHandler.supports(decoder, decode)) {
                    continue;
                }
                try {
                    channelReadableHandler.channelRead(ctx);
                } catch (ClassCastException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Got an ClassCastException while channelRead() in selector thread [{}]!", super.getName(), e);
                    }
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

    public void wakeup() {
        selector.wakeup();
    }

    /**
     * 清除掉操作操作失败的SelectionKey
     *
     * @param selectionKey 要清除的selectionKey
     */
    protected void cleanupSelectionKey(SelectionKey selectionKey) {
        FrameBuffer buffer = (FrameBuffer) selectionKey.attachment();
        if (buffer != null) {
            buffer.close();
        }
        selectionKey.cancel();
    }
    /**
     * 处理
     */
    public void processSelectInterestReadChange(FrameBuffer frameBuffer) {
        synchronized (selectInterestReadChanges) {
            selectInterestReadChanges.add(frameBuffer);
        }
        selector.wakeup();
    }

    public void processSelectInterestWriteChange(FrameBuffer frameBuffer) {
        synchronized (selectInterestWriteChanges) {
            selectInterestWriteChanges.add(frameBuffer);
        }
        selector.wakeup();
    }

    protected void processInterestChanges() {
        synchronized (selectInterestReadChanges) {
            for (FrameBuffer fb : selectInterestReadChanges) {
                fb.changeSelectInterests(false);
            }
            selectInterestReadChanges.clear ();
        }
        synchronized (selectInterestWriteChanges) {
            for (FrameBuffer fb : selectInterestWriteChanges) {
                fb.changeSelectInterests(true);
            }
            selectInterestWriteChanges.clear ();
        }
    }

    /**
     * 获取对应SocketChannel的FrameBuffer
     * @param socketChannel     要获取buffer的SocketChannel
     * @return {@code null}     SocketChannel不匹配
     */
    abstract public FrameBuffer getBuffer(SocketChannel socketChannel);
}
