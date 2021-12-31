package com.future94.gothrough.protocol.nio.server;

import com.future94.gothrough.common.utils.ByteBufferUtils;
import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.nio.handler.TestChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.codec.Encoder;
import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectEncoder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author weilai
 */
class GoThroughNioServerTest {

    public static void main(String[] args) throws IOException {
        GoThroughNioServer server = new GoThroughNioServer();
        server.setSelectorThreadCount(1);
        server.setReadableHandler(new TestChannelReadableHandler());
        server.start(10010);
    }

    @Test
    public void clientConnect() throws Exception {
        SocketChannel socketChannel = SocketUtils.createSocketChannel("127.0.0.1", 10010);
        socketChannel.configureBlocking(false);
        Encoder encoder = new ObjectEncoder();
        byte[] payload = encoder.encode("hello word!");
        ByteBufferUtils.channelWrite(socketChannel, ByteBuffer.wrap(ByteBufferUtils.intToBytes(payload.length)));
        ByteBufferUtils.channelWrite(socketChannel, ByteBuffer.wrap(payload));
    }
}