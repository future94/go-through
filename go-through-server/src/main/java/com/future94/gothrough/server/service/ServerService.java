package com.future94.gothrough.server.service;

import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.server.adapter.ClientSocketAdapter;

import java.io.IOException;
import java.net.Socket;

/**
 * @author weilai
 */
public interface ServerService<R, W> {

    ClientSocketAdapter createClientSocketAdapter();

    GoThroughSocketChannel<R, W> createGoThroughSocketChannel(Socket socket) throws IOException;

}
