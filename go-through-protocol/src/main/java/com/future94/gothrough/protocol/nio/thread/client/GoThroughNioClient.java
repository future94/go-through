package com.future94.gothrough.protocol.nio.thread.client;

import com.future94.gothrough.protocol.nio.thread.AbstractProcess;
import com.future94.gothrough.protocol.nio.thread.client.thread.ClientSelectThread;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * Nio客户端
 * @author weilai
 */
@Slf4j
public class GoThroughNioClient extends AbstractProcess implements NioClient {

    /**
     * 连接服务端状态
     */
    private volatile boolean connected = false;

    /**
     * 启动状态
     */
    private volatile boolean started = false;

    /**
     * 是否使用编解码器
     */
    private boolean useCodec = true;

    /**
     * 服务端IP
     */
    @Getter
    @Setter
    private String ip;

    /**
     * 服务端端口
     */
    @Getter
    @Setter
    private int port;

    /**
     * 处理{@link java.nio.channels.SocketChannel}的{@link java.nio.channels.SelectionKey#OP_READ}和{@link java.nio.channels.SelectionKey#OP_WRITE}事件的线程
     */
    private ClientSelectThread selectorThread;

    @Override
    public void connect() throws IOException {
        this.connect(this.ip, this.port);
    }

    @Override
    public void connect(String ip, int port) throws IOException {
        if (Objects.isNull(ip)) {
            throw new IllegalArgumentException("start go through nio client error, because ip is null");
        }
        this.ip = ip;
        if (port <= 0) {
            throw new IllegalArgumentException("start go through nio client error, because port is illegal");
        }
        this.port = port;
        ClientSelectThread selectorThread = new ClientSelectThread(this);
        selectorThread.start();
        this.selectorThread = selectorThread;
        this.started = true;
    }

    @Override
    public boolean writeChannel(Object msg) {
        return this.selectorThread.write(msg);
    }

    @Override
    public void close() throws IOException {
        this.started = false;
        selectorThread.interrupt();
    }

    @Override
    public void setConnected() {
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public SocketChannel getSocketChannel() {
        return this.selectorThread.getSocketChannel();
    }
}
