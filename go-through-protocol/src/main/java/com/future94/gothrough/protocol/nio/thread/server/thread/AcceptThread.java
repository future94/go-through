package com.future94.gothrough.protocol.nio.thread.server.thread;

import com.future94.gothrough.protocol.nio.thread.server.GoThroughNioServer;
import com.future94.gothrough.protocol.nio.thread.server.NioServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

/**
 * 处理Accept事件线程
 * @author weilai
 */
@Slf4j
public class AcceptThread extends Thread {

    /**
     * Accept的选择
     */
    private final Selector selector;

    /**
     * 所属的Server
     */
    private final NioServer server;

    public AcceptThread(GoThroughNioServer server) throws IOException {
        this("Accept-Thread-Listen-" + server.getPort(), server);
    }

    AcceptThread(String threadName, NioServer server) throws IOException {
        super.setName(threadName);
        this.selector = SelectorProvider.provider().openSelector();
        this.server = server;
        ServerSocketChannel serverSocketChannel = SelectorProvider.provider().openServerSocketChannel();
        serverSocketChannel.bind(new InetSocketAddress(server.getPort()));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            for (; server.isStart(); ) {
                select(selector);
            }
        } catch (Throwable e) {
            log.error("The thread [{}] processing select method throws IOException ", getName(), e);
        } finally {
            server.stop();
        }
    }

    /**
     * 处理Selector事件
     * @param selector 要处理事件的选择器
     */
    private void select(Selector selector) {
        try {
            int select = selector.select();
            if (select <= 0) {
                return;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (server.isStart() && iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (!selectionKey.isValid()) {
                    continue;
                }
                // Accept事件准备就绪
                if (selectionKey.isAcceptable()) {
                    handleAccept(selectionKey);
                } else {
                    // 在AcceptThread中select方法出现意外状态
                    log.warn("An unexpected state [{}] occurred in the select method on Accept Thread", selectionKey.interestOps());
                }
            }
        } catch (IOException e) {
            log.error("Got an IOException while selecting in accept thread [{}]!", super.getName(), e);
        }
    }

    /**
     * 处理{@link SelectionKey#OP_ACCEPT}事件
     */
    private void handleAccept(SelectionKey selectionKey) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            ServerSelectorThread selectorThread = server.chooseSelectorThread();
            selectorThread.addQueue(socketChannel);
        } catch (IOException e) {
            log.error("Got an IOException while handleAccept() in accept thread [{}]!", super.getName(), e);
        }
    }
}