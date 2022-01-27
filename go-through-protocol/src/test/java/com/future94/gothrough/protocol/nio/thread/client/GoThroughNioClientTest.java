package com.future94.gothrough.protocol.nio.thread.client;

import com.future94.gothrough.common.utils.ByteBufferUtils;
import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.nio.handler.TestByteChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.TestPrintChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.codec.Encoder;
import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectEncoder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author weilai
 */
class GoThroughNioClientTest {

    public static void main(String[] args) throws Exception {
       byteArrayClient();
    }

    private static void objectClient() throws IOException {
        GoThroughNioClient client = new GoThroughNioClient();
        client.setReadableHandler(new TestPrintChannelReadableHandler());
        client.connect("127.0.0.1", 10010);
        client.writeChannel("hello");
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String nextLine = scanner.nextLine();
            client.writeChannel(nextLine);
        }
    }

    private static void byteArrayClient() throws IOException {
        GoThroughNioClient client = new GoThroughNioClient();
        client.connect("127.0.0.1", 10010);
        client.writeChannel("hello".getBytes(StandardCharsets.UTF_8));
        client.setReadableHandler(new TestByteChannelReadableHandler());
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String nextLine = scanner.nextLine();
            client.writeChannel(nextLine.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void syncSendToServer() throws Exception {
        SocketChannel socketChannel = SocketUtils.createBlockSocketChannel("127.0.0.1", 10010);
        Encoder encoder = new ObjectEncoder();
        byte[] payload = encoder.encode("hello word!");
        ByteBufferUtils.channelWrite(socketChannel, ByteBuffer.wrap(ByteBufferUtils.intToBytes(payload.length)));
        ByteBufferUtils.channelWrite(socketChannel, ByteBuffer.wrap(payload));
    }
}