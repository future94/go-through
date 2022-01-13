package com.future94.gothrough.protocol.nio.thread.server;

import com.future94.gothrough.protocol.nio.buffer.FrameBuffer;
import com.future94.gothrough.protocol.nio.handler.AcceptHandler;
import com.future94.gothrough.protocol.nio.thread.AbstractProcess;
import com.future94.gothrough.protocol.nio.thread.server.thread.AcceptThread;
import com.future94.gothrough.protocol.nio.thread.server.thread.ServerSelectorThread;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author weilai
 */
@Slf4j
public class GoThroughNioServer extends AbstractProcess implements NioServer {

    /**
     * 是否初始化
     */
    private volatile boolean created = false;

    /**
     * Server启动状态
     */
    private volatile boolean started = false;

    /**
     * 处理{@link java.nio.channels.ServerSocketChannel}的{@link java.nio.channels.SelectionKey#OP_ACCEPT}事件的线程
     */
    @Getter
    private AcceptThread acceptThread;

    /**
     * 处理{@link java.nio.channels.SocketChannel}的{@link java.nio.channels.SelectionKey#OP_READ}和{@link java.nio.channels.SelectionKey#OP_WRITE}事件的线程
     */
    @Getter
    private Set<ServerSelectorThread> selectorThreads = new HashSet<>();

    /**
     * 选择一个合适的SelectorThread
     * 简单的轮询负载实现
     */
    private Iterator<ServerSelectorThread> selectorThreadLoadBalancer;

    /**
     * 当{@link java.nio.channels.SelectionKey#OP_ACCEPT}事件的回调
     */
    @Getter
    private List<AcceptHandler> acceptHandlers = new ArrayList<>();

    /**
     * 监听的接口
     */
    private Integer port;

    /**
     * {@link ServerSelectorThread}线程数
     */
    private int selectorThreadCount = 1 << 2;

    @Override
    public Integer getPort() {
        return this.port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setSelectorThreadCount(int selectorThreadCount) {
        if (selectorThreadCount < 1) {
            throw new IllegalArgumentException("selectorThreadCount must be greater than 0.");
        }
        this.selectorThreadCount = selectorThreadCount;
    }

    @Override
    public void create() throws IOException {
        this.acceptThread = new AcceptThread(this);
        if (this.selectorThreadCount == 1) {
            this.selectorThreads.add(new ServerSelectorThread(this));
        } else {
            for (int i = 1; i <= this.selectorThreadCount; i++) {
                this.selectorThreads.add(new ServerSelectorThread("Selector-Thread-Listen-" + this.getPort() + "-" + i, this));
            }
        }
        this.selectorThreadLoadBalancer = this.selectorThreads.iterator();
        this.created = true;
    }

    @Override
    public boolean start() throws IOException {
        Integer port = this.port;
        if (Objects.isNull(port)) {
            throw new RuntimeException("start go through nio server error, because port is null");
        }
        return this.start(port);
    }

    @Override
    public boolean start(int port) throws IOException {
        this.port = port;
        if (!created) {
            this.create();
        }
        this.acceptThread.start();
        // 需要在Select线程启动前修改
        this.started = true;
        for (ServerSelectorThread selectorThread : this.selectorThreads) {
            selectorThread.start();
        }
        log.info("start nio server success, listen port [{}]", port);
        return true;
    }

    @Override
    public void stop() {
        this.started = false;
        for (ServerSelectorThread selectorThread : this.selectorThreads) {
            selectorThread.interrupt();
        }
    }

    @Override
    public boolean isStart() {
        return started;
    }

    @Override
    public void setAcceptHandler(AcceptHandler acceptHandler) {
        this.acceptHandlers.add(acceptHandler);
    }

    /**
     * 轮询选择一个{@link ServerSelectorThread}.
     */
    public ServerSelectorThread chooseSelectorThread() {
        Iterator<ServerSelectorThread> iterator = this.selectorThreadLoadBalancer;
        if (!iterator.hasNext()) {
            iterator = this.selectorThreadLoadBalancer = selectorThreads.iterator();
        }
        return iterator.next();
    }

    @Override
    public void writeChannel(SocketChannel socketChannel, Object msg) throws Exception {
        Set<ServerSelectorThread> selectorThreads = getSelectorThreads();
        boolean matching = false;
        for (ServerSelectorThread selectorThread : selectorThreads) {
            FrameBuffer frameBuffer = selectorThread.getBuffer(socketChannel);
            if (frameBuffer != null) {
                matching = true;
                frameBuffer.writeBuffer(msg);
                selectorThread.wakeup();
            }
        }
        if (!matching && log.isWarnEnabled()) {
            log.warn("Failed to write data [{}] to the socket channel [{}]", msg.toString(), socketChannel.toString());
        }
    }
}
