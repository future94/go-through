package cn.gothrough.server.context;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

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
     * key:     listenPort
     * value:   ClientProxyChannel
     */
    private static final Map<Integer, Channel> CLIENT_PROXY_CHANNEL_CACHE = new ConcurrentHashMap<>();

    /**
     * key:     ClientKey
     * value:   ClientChannel
     */
    private static final Map<String, Channel> KEY_CLIENT_CHANNEL_CACHE = new ConcurrentHashMap<>();

    /**
     * key:     listenPort
     * value:   与客户端是否交互成功
     */
    private static final Map<Integer, Boolean> CONNECTED_CACHE = new ConcurrentHashMap<>();

    public static Channel getClientChannel(Integer listenPort) {
        return PORT_CLIENT_CHANNEL_CACHE.get(listenPort);
    }

    public static void setClientChannel(Integer listenPort, Channel ClientChannel) {
        PORT_CLIENT_CHANNEL_CACHE.put(listenPort, ClientChannel);
    }

    public static Channel getClientProxyChannel(Integer listenPort) {
        return CLIENT_PROXY_CHANNEL_CACHE.get(listenPort);
    }

    public static void setClientProxyChannel(Integer listenPort, Channel clientProxyChannel) {
        CLIENT_PROXY_CHANNEL_CACHE.put(listenPort, clientProxyChannel);
    }

    public static Channel getClientChannel(String key) {
        return KEY_CLIENT_CHANNEL_CACHE.get(key);
    }

    public static void setClientChannel(String key, Channel channel) {
        KEY_CLIENT_CHANNEL_CACHE.put(key, channel);
    }

    public static void setConnected(Integer listenPort, Boolean connected) {
        CONNECTED_CACHE.put(listenPort, connected);
    }

    public static Boolean isConnected(Integer listenPort) {
        return CONNECTED_CACHE.getOrDefault(listenPort, false);
    }
}
