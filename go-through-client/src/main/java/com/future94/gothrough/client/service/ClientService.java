package com.future94.gothrough.client.service;

import com.future94.gothrough.client.adapter.ClientAdapter;
import com.future94.gothrough.client.thread.ClientThreadManager;
import com.future94.gothrough.client.thread.Heartbeat;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.part.SocketPart;

/**
 * @author weilai
 */
public interface ClientService<R, W> {

    /**
     * 创建心跳线程
     */
    Heartbeat createHeartbeatThread(ClientThreadManager clientThread);

    /**
     * 创建适配器
     */
    ClientAdapter<R, W> createControlAdapter(ClientThreadManager clientThread);

    SocketPart createSocketPart(ClientThreadManager clientThread);

    /**
     * 创建GoThroughSocketChannel
     */
    GoThroughSocketChannel<R, W> createGoThroughSocketChannel(String ip, Integer port);
}
