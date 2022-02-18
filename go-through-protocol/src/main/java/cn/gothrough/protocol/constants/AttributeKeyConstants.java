package cn.gothrough.protocol.constants;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;


/**
 * @author weilai
 */
public interface AttributeKeyConstants {

    /**
     * 代理Channel
     *
     * client端代理Channel为与server端建立连接的Channel，向这个Channel写数据相当于向ServerChannel发送数据
     * server端代理Channel为
     */
    AttributeKey<Channel> PROXY_CHANNEL = AttributeKey.newInstance("proxy_channel");

    /**
     * 客户端与服务端建立的Channel
     */
    AttributeKey<Channel> CLIENT_SERVER_CHANNEL = AttributeKey.newInstance("client_server_channel");

    /**
     * 客户端与内部要暴漏建立的Channel
     */
    AttributeKey<Channel> CLIENT_INTRANET_CHANNEL = AttributeKey.newInstance("client_intranet_channel");

    /**
     * 客户端转发的Channel（用来服务端监听的ListenChannel找到对应的转发的客户端Channel）
     */
    AttributeKey<Channel> CLIENT_PROXY_CHANNEL = AttributeKey.newInstance("client_proxy_channel");


    /**
     * 服务端代理端口（转发客户端内部）
     */
    AttributeKey<List<Integer>> PROXY_PORT = AttributeKey.newInstance("proxy_port");

    /**
     * 客户端key
     */
    AttributeKey<String> CLIENT_KEY = AttributeKey.newInstance("client_key");

    /**
     * 客户端要暴漏的主机地址（host:ip）
     */
    AttributeKey<String> CLIENT_INTRANET_HOSTNAME = AttributeKey.newInstance("client_intranet_hostname");

    /**
     * 服务端监听生成的ID
     */
    AttributeKey<String> SERVER_LISTEN_ID = AttributeKey.newInstance("server_listen_id");

    /**
     * 服务端监听到的ID与监听到的Channel映射关系
     * key      {@link #SERVER_LISTEN_ID}
     * value    {@link Channel} 请求Accept到的Channel
     */
    AttributeKey<Map<String, Channel>> SERVER_LISTEN_CHANNEL_MAPPING = AttributeKey.newInstance("server_listen_channel_mapping");

    /**
     * 服务端监听到的要转发Channel
     */
    AttributeKey<Channel> SERVER_LISTEN_CHANNEL = AttributeKey.newInstance("server_listen_channel");
}
