package com.future94.gothrough.protocol.nio.thread;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;
import com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.codec.Decoder;
import com.future94.gothrough.protocol.nio.handler.codec.Encoder;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author weilai
 */
@Slf4j
public abstract class AbstractSelectThread extends Thread {

    /**
     * 事件选择器
     */
    protected final Selector selector;

    protected final Set<FrameBuffer> selectInterestChanges = new HashSet<>();

    /**
     * 当{@link java.nio.channels.SelectionKey#OP_READ}事件的回调
     */
    private final List<ChannelReadableHandler> channelReadableHandlers;

    @Getter
    private final Encoder encoder;

    private final Decoder<?> decoder;

    public AbstractSelectThread(List<ChannelReadableHandler> channelReadableHandlers, Encoder encoder, Decoder<?> decoder) throws IOException {
        this.channelReadableHandlers = channelReadableHandlers;
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
    private void handleRead(SelectionKey selectionKey) {
        FrameBuffer buffer = (FrameBuffer) selectionKey.attachment();
        if (!buffer.read()) {
            cleanupSelectionKey(selectionKey);
            return;
        }
        if (buffer.isReadCompleted()) {
            if (!doReadableHandler(selectionKey)) {
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
     * 向SocketChanel写入数据
     */
    public boolean write(SocketChannel socketChannel, Object msg) {
        FrameBuffer buffer = getBuffer(socketChannel);
        if (buffer == null) {
            log.warn("Failed to write data [{}] to the socket channel [{}]", msg.toString(), socketChannel.toString());
            return false;
        }
        return buffer.writeBuffer(msg);
    }

    /**
     * 回调{@link ChannelReadableHandler}处理器
     *
     * @param selectionKey 已经读取好数据的buffer
     * @return {@code true} 回调成功
     */
    private boolean doReadableHandler(SelectionKey selectionKey) {
        FrameBuffer buffer = (FrameBuffer) selectionKey.attachment();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        try {
            ChannelHandlerContext ctx = new ChannelHandlerContext(buffer, socketChannel);
            for (ChannelReadableHandler channelReadableHandler : channelReadableHandlers) {
                Object decode = decoder.decode(buffer);
                if (!channelReadableHandler.supports(decode)) {
                    continue;
                }
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("selector thread [{}] recv message :[{}]", super.getName(), decode.toString());
                    }
                    channelReadableHandler.channelRead(ctx, decode);
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
    public void processSelectInterestChange(FrameBuffer frameBuffer) {
        synchronized (selectInterestChanges) {
            selectInterestChanges.add(frameBuffer);
        }
        selector.wakeup();
    }

    protected void processInterestChanges() {
        synchronized (selectInterestChanges) {
            for (FrameBuffer fb : selectInterestChanges) {
                fb.changeSelectInterests ();
            }
            selectInterestChanges.clear ();
        }
    }

    public abstract SelectionKey prepareWriteBuffer(SelectionKey selectionKey) throws ClosedChannelException;

    /**
     * 获取对应SocketChannel的FrameBuffer
     * @param socketChannel     要获取buffer的SocketChannel
     * @return {@code null}     SocketChannel不匹配
     */
    abstract public FrameBuffer getBuffer(SocketChannel socketChannel);
}
