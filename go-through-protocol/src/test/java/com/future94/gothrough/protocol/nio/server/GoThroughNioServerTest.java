package com.future94.gothrough.protocol.nio.server;

import com.future94.gothrough.common.utils.ByteBufferUtils;
import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.nio.handler.TestPrintChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.codec.Encoder;
import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectDecoder;
import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectEncoder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.locks.LockSupport;

/**
 * @author weilai
 */
class GoThroughNioServerTest {


    @Test
    public void start() throws IOException {
        GoThroughNioServer server = new GoThroughNioServer();
        server.setSelectorThreadCount(1);
        server.setReadableHandler(new TestPrintChannelReadableHandler());
        server.setWritableHandler(() -> "success");
        server.start(10010);
        LockSupport.park();
    }

    @Test
    public void clientConnect() throws Exception {
        SocketChannel socketChannel = SocketUtils.createSocketChannel("127.0.0.1", 10010, false);
        socketChannel.configureBlocking(false);
        Selector selector = SelectorProvider.provider().openSelector();
        socketChannel.register(selector, SelectionKey.OP_READ);
        Encoder encoder = new ObjectEncoder();
        byte[] payload = encoder.encode("hello word!");
        ByteBufferUtils.channelWrite(socketChannel, ByteBuffer.wrap(ByteBufferUtils.intToBytes(payload.length)));
        ByteBufferUtils.channelWrite(socketChannel, ByteBuffer.wrap(payload));
        while (true) {
            int select = selector.select();
            if (select <= 0) {
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            if (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (!selectionKey.isValid()) {
                    continue;
                }
                if (selectionKey.isReadable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1 << 12);
                    channel.read(byteBuffer);
                    byteBuffer.flip();
                    Object decode = new ObjectDecoder().decode(byteBuffer.array());
                    System.out.println(decode.toString());
                }
            }
        }
    }
}