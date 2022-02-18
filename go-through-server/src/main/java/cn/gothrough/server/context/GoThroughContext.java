package cn.gothrough.server.context;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilai
 */
public class GoThroughContext {

    /**
     * key:     listenPort
     * value:   ClientChannel
     */
    private static final Map<Integer, Channel> PORT_CLIENT_CHANNEL_CACHE = new ConcurrentHashMap<>();

    /**
     * key:     ClientKey
     * value:   ClientChannel
     */
    private static final Map<String, Channel> KEY_CLIENT_CHANNEL_CACHE = new ConcurrentHashMap<>();

    public static Channel getClientChannel(Integer port) {
        return PORT_CLIENT_CHANNEL_CACHE.get(port);
    }

    public static void setClientChannel(Integer port, Channel channel) {
        PORT_CLIENT_CHANNEL_CACHE.put(port, channel);
    }

    public static Channel getClientChannel(String key) {
        return KEY_CLIENT_CHANNEL_CACHE.get(key);
    }

    public static void setClientChannel(String key, Channel channel) {
        KEY_CLIENT_CHANNEL_CACHE.put(key, channel);
    }
}
