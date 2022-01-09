package com.future94.gothrough.client.cache;

import com.future94.gothrough.client.thread.ClientThreadManager;
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
public class ClientThreadCache {

    private static final ConcurrentHashMap<Integer, ClientThreadManager> CLIENT_THREAD_CACHE = new ConcurrentHashMap<>();

    public static void add(ClientThreadManager serverListenThreadManager) {
        if (serverListenThreadManager == null) {
            return;
        }
        if (Objects.nonNull(CLIENT_THREAD_CACHE.get(serverListenThreadManager.getServerExposedListenPort()))) {
            log.warn("serverExposedListenPort[{}]已经存在", serverListenThreadManager.getServerExposedListenPort());
        }
        CLIENT_THREAD_CACHE.put(serverListenThreadManager.getServerExposedListenPort(), serverListenThreadManager);
    }

    public static void remove(Integer listenPort) {
        Optional.ofNullable(CLIENT_THREAD_CACHE.remove(listenPort)).ifPresent(ClientThreadManager::cancel);
    }

    public static ClientThreadManager get(Integer listenPort) {
        return CLIENT_THREAD_CACHE.get(listenPort);
    }

    public static List<ClientThreadManager> getAll() {
        return new LinkedList<>(CLIENT_THREAD_CACHE.values());
    }

    public static void closeAll() {
        Integer[] array = CLIENT_THREAD_CACHE.keySet().toArray(new Integer[0]);
        for (Integer key : array) {
            remove(key);
        }
    }
}
