package com.future94.gothrough.server.adapter;

import java.net.Socket;

/**
 * @author weilai
 */
public interface ClientSocketAdapter {

    void process(Socket acceptClientSocket) throws Exception;
}
