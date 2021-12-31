package com.future94.gothrough.protocol.nio.server;

import com.future94.gothrough.protocol.nio.handler.AcceptHandler;
import com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.ChannelWritableHandler;
import com.future94.gothrough.protocol.nio.handler.codec.Decoder;
import com.future94.gothrough.protocol.nio.handler.codec.Encoder;
import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectDecoder;
import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectEncoder;
import com.future94.gothrough.protocol.nio.thread.AcceptThread;
import com.future94.gothrough.protocol.nio.thread.SelectorThread;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
public class GoThroughNioServer implements NioServer {

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
    private Set<SelectorThread> selectorThreads = new HashSet<>();

    /**
     * 选择一个合适的SelectorThread
     * 简单的轮询负载实现
     */
    private Iterator<SelectorThread> selectorThreadLoadBalancer;

    @Getter
    @Setter
    private Encoder encoder = new ObjectEncoder();

    @Getter
    @Setter
    private Decoder<?> decoder = new ObjectDecoder();

    /**
     * 当{@link java.nio.channels.SelectionKey#OP_ACCEPT}事件的回调
     */
    @Getter
    private List<AcceptHandler> acceptHandlers = new ArrayList<>();

    /**
     * 当{@link java.nio.channels.SelectionKey#OP_READ}事件的回调
     */
    @Getter
    private List<ChannelReadableHandler> channelReadableHandlers = new ArrayList<>();

    /**
     * 当{@link java.nio.channels.SelectionKey#OP_WRITE}事件的回调
     */
    @Getter
    private ChannelWritableHandler channelWritableHandler;

    /**
     * 监听的接口
     */
    private Integer port;

    /**
     * {@link SelectorThread}线程数
     */
    private int selectorThreadCount = 1 << 2;

    @Getter
    private byte[] writePayload;

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
    public void setWriteData(String payload) {
        this.setWriteData(payload.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void setWriteData(byte[] payload) {
        this.writePayload = payload;
    }

    @Override
    public void create() throws IOException {
        this.acceptThread = new AcceptThread(this);
        if (this.selectorThreadCount == 1) {
            this.selectorThreads.add(new SelectorThread(this));
        } else {
            for (int i = 1; i <= this.selectorThreadCount; i++) {
                this.selectorThreads.add(new SelectorThread("Selector-Thread-Listen-" + this.getPort() + "-" + i, this));
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
        this.started = true;
        this.acceptThread.start();
        for (SelectorThread selectorThread : this.selectorThreads) {
            selectorThread.start();
        }
        return true;
    }

    @Override
    public void stop() {
        this.started = false;
    }

    @Override
    public boolean isStart() {
        return started;
    }

    @Override
    public void setAcceptHandler(AcceptHandler acceptHandler) {
        this.acceptHandlers.add(acceptHandler);
    }

    @Override
    public void setReadableHandler(ChannelReadableHandler channelReadableHandler) {
        this.channelReadableHandlers.add(channelReadableHandler);
    }

    @Override
    public void setWritableHandler(ChannelWritableHandler channelWritableHandler) {
        this.channelWritableHandler = channelWritableHandler;
    }

    public SelectorThread chooseSelectorThread() {
        Iterator<SelectorThread> iterator = this.selectorThreadLoadBalancer;
        if (!iterator.hasNext()) {
            iterator = this.selectorThreadLoadBalancer = selectorThreads.iterator();
        }
        return iterator.next();
    }
}
