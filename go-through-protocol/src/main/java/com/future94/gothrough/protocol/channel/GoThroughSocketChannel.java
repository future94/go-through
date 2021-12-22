package com.future94.gothrough.protocol.channel;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * @author weilai
 */
public interface GoThroughSocketChannel<R, W> extends Closeable {

    R read() throws Exception;

    void write(W value) throws Exception;

    void flush() throws Exception;

    void writeAndFlush(W value) throws Exception;

    Socket getSocket();

    void setSocket(Socket socket) throws IOException;
}
