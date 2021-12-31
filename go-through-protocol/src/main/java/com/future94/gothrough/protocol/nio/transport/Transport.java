package com.future94.gothrough.protocol.nio.transport;

import com.future94.gothrough.common.exception.TransportException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * 交换数据传输
 * @author weilai
 */
@Slf4j
public class Transport {

    private SocketChannel socketChannel;

    public Transport(SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(false);
        this.socketChannel = socketChannel;
    }

    public SelectionKey registerSelector(Selector selector, int ops) throws IOException {
        return this.socketChannel.register(selector, ops);
    }

    public int read(ByteBuffer buffer) throws IOException {
        return this.socketChannel.read(buffer);
    }

    public int read(byte[] buf, int off, int len) throws TransportException {
        if ((this.socketChannel.validOps() & SelectionKey.OP_READ) != SelectionKey.OP_READ) {
            throw new TransportException("Cannot read from write-only socket channel");
        } else {
            try {
                return this.socketChannel.read(ByteBuffer.wrap(buf, off, len));
            } catch (IOException e) {
                throw new TransportException(e);
            }
        }
    }

    public int write(ByteBuffer buffer) throws IOException {
        return this.socketChannel.write(buffer);
    }

    public void write(byte[] buf, int off, int len) throws TransportException {
        if ((this.socketChannel.validOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
            throw new TransportException("Cannot write to write-only socket channel");
        } else {
            try {
                this.socketChannel.write(ByteBuffer.wrap(buf, off, len));
            } catch (IOException e) {
                throw new TransportException(e);
            }
        }
    }
}
