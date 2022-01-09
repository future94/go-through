package com.future94.gothrough.common.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * @author weilai
 */
public class SocketUtils {

    public static SocketChannel createSocketChannel(String ip, Integer port, boolean block) throws IOException {
        SocketChannel openSocketChannel = SelectorProvider.provider().openSocketChannel();
        openSocketChannel.configureBlocking(block);
        openSocketChannel.connect(new InetSocketAddress(ip, port));
        return openSocketChannel;
    }

    public static Socket createSocket(String ip, Integer port) throws IOException {
        SocketChannel openSocketChannel = SelectorProvider.provider().openSocketChannel();
        openSocketChannel.connect(new InetSocketAddress(ip, port));
        return openSocketChannel.socket();
    }

    public static ServerSocket createServerSocket(Integer port) throws IOException {
        ServerSocketChannel openServerSocketChannel = SelectorProvider.provider().openServerSocketChannel();
        openServerSocketChannel.bind(new InetSocketAddress(port));
        return openServerSocketChannel.socket();
    }
}
