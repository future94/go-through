package com.future94.gothrough.protocol.nio.thread.server;

import com.future94.gothrough.protocol.nio.handler.TestPrintWriteBackChannelReadableHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

/**
 * @author weilai
 */
class GoThroughNioServerTest {

    @Test
    public void serverStart() throws IOException {
        GoThroughNioServer server = new GoThroughNioServer();
        server.setSelectorThreadCount(1);
        server.setReadableHandler(new TestPrintWriteBackChannelReadableHandler());
        server.start(10010);
        LockSupport.park();
    }

    public static void main(String[] args) throws IOException {
        GoThroughNioServer server = new GoThroughNioServer();
        server.setSelectorThreadCount(1);
        server.setReadableHandler(new TestPrintWriteBackChannelReadableHandler());
        server.start(10010);
        System.out.println("success");
    }
}