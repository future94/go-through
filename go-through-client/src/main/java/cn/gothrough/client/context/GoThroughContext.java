package cn.gothrough.client.context;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilai
 */
public class GoThroughContext {

    /**
     * 与Boss服务端建立连接的Channel
     */
    private static Channel bossServerChannel;

    /**
     * key:     serverListenID
     * value:   Channel(与内网建立连接的Channel)
     */
    private static final Map<String, Channel> SERVER_LISTEN_ID_INTRANET_CHANNEL_MAPPING_CHACHE = new ConcurrentHashMap<>();

    public static void setBossServerChannel(Channel bossServerChannel) {
        GoThroughContext.bossServerChannel = bossServerChannel;
    }

    public static Channel getBossServerChannel() {
        return bossServerChannel;
    }

    public static void addIntranetChannel(String serverListenId, Channel internetChannel) {
        SERVER_LISTEN_ID_INTRANET_CHANNEL_MAPPING_CHACHE.put(serverListenId, internetChannel);
    }

    public static Channel getInternetChannel(String serverListenId) {
        return SERVER_LISTEN_ID_INTRANET_CHANNEL_MAPPING_CHACHE.get(serverListenId);
    }

    public static Channel removeIntranetChannel(String serverListenId) {
        return SERVER_LISTEN_ID_INTRANET_CHANNEL_MAPPING_CHACHE.remove(serverListenId);
    }

    public static void clearIntranetChannels() {
        Iterator<Map.Entry<String, Channel>> iterator = SERVER_LISTEN_ID_INTRANET_CHANNEL_MAPPING_CHACHE.entrySet().iterator();
        if (iterator.hasNext()) {
            Channel intranetChannel = iterator.next().getValue();
            if (intranetChannel != null && intranetChannel.isActive()) {
                intranetChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
        SERVER_LISTEN_ID_INTRANET_CHANNEL_MAPPING_CHACHE.clear();
    }
}
