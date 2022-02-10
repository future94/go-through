package com.future94.gothrough.server.cache;

import com.future94.gothrough.server.boss.thread.ServerThreadManager;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilai
 */
public class GoThroughContextHolder {

    private static volatile ServerThreadManager serverThreadManager;

    private static volatile Map<Integer, ServerListenThreadManager> serverListenThreadManagerMap = new ConcurrentHashMap<>();

    private static volatile Map<Integer, SocketChannel> clientControlSocketChannelMap = new ConcurrentHashMap<>();

    public static ServerThreadManager getServerThreadManager() {
        return serverThreadManager;
    }

    public static void setServerThreadManager(ServerThreadManager serverThreadManager) {
        GoThroughContextHolder.serverThreadManager = serverThreadManager;
    }

    public static ServerListenThreadManager getServerListenThreadManager(Integer listenPort) {
        return serverListenThreadManagerMap.get(listenPort);
    }

    public static void setServerListenThreadManager(ServerListenThreadManager serverListenThreadManager) {
        serverListenThreadManagerMap.put(serverListenThreadManager.getListenPort(), serverListenThreadManager);
    }

    public static ServerListenThreadManager removeServerListenThreadManager(Integer listenPort) {
        return serverListenThreadManagerMap.remove(listenPort);
    }

    public static SocketChannel getClientControlSocketChannel(Integer listenPort) {
        return clientControlSocketChannelMap.get(listenPort);
    }

    public static void setClientControlSocketChannel(Integer listenPort, SocketChannel socketChannel) {
        clientControlSocketChannelMap.put(listenPort, socketChannel);
    }
}
