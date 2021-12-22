package com.future94.gothrough.server.listen.cache;

import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilai
 */
@Slf4j
public class ServerListenThreadCache {

    private static final ConcurrentHashMap<Integer, ServerListenThreadManager> SERVER_LISTEN_THREAD_CACHE = new ConcurrentHashMap<>();

    public static void add(ServerListenThreadManager serverListenThreadManager) {
        if (serverListenThreadManager == null) {
            return;
        }
        if (Objects.nonNull(SERVER_LISTEN_THREAD_CACHE.get(serverListenThreadManager.getListenPort()))) {
            log.warn("listenPort[{}]已经存在", serverListenThreadManager.getListenPort());
        }
        SERVER_LISTEN_THREAD_CACHE.put(serverListenThreadManager.getListenPort(), serverListenThreadManager);
    }

    public static void remove(Integer listenPort) {
        Optional.ofNullable(SERVER_LISTEN_THREAD_CACHE.remove(listenPort)).ifPresent(ServerListenThreadManager::cancel);
    }

    public static ServerListenThreadManager get(Integer listenPort) {
        return SERVER_LISTEN_THREAD_CACHE.get(listenPort);
    }

    public static List<ServerListenThreadManager> getAll() {
        return new LinkedList<>(SERVER_LISTEN_THREAD_CACHE.values());
    }

    public static void closeAll() {
        Integer[] array = SERVER_LISTEN_THREAD_CACHE.keySet().toArray(new Integer[0]);
        for (Integer key : array) {
            remove(key);
        }
    }
}
