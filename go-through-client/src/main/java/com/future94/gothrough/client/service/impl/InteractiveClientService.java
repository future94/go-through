package com.future94.gothrough.client.service.impl;

import com.future94.gothrough.client.adapter.ClientAdapter;
import com.future94.gothrough.client.adapter.InteractiveClientAdapter;
import com.future94.gothrough.client.handler.CommonReplyHandler;
import com.future94.gothrough.client.handler.HeartBeatHandler;
import com.future94.gothrough.client.handler.ServerWaitClientHandler;
import com.future94.gothrough.client.service.ClientService;
import com.future94.gothrough.client.thread.ClientThreadManager;
import com.future94.gothrough.client.thread.Heartbeat;
import com.future94.gothrough.client.thread.HeartbeatThread;
import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.channel.InteractiveChannel;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.part.InteractiveSocketPart;
import com.future94.gothrough.protocol.part.SocketPart;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

/**
 * @author weilai
 */
@Slf4j
public class InteractiveClientService implements ClientService<InteractiveModel, InteractiveModel> {

    @Override
    public Heartbeat createHeartbeatThread(ClientThreadManager clientThread) {
        HeartbeatThread heartbeatThread = new HeartbeatThread(clientThread);
        heartbeatThread.setHeartIntervalSeconds(100);
        heartbeatThread.setMaxRetryConnectCount(100);
        return heartbeatThread;
    }

    @Override
    public ClientAdapter<InteractiveModel, InteractiveModel> createControlAdapter(ClientThreadManager clientThread) {
        InteractiveClientAdapter clientAdapter = new InteractiveClientAdapter(clientThread, this);
        clientAdapter.setPipelineList(CommonReplyHandler.getInstance());
        clientAdapter.setPipelineList(HeartBeatHandler.getInstance());
        clientAdapter.setPipelineList(ServerWaitClientHandler.getInstance());
        return clientAdapter;
    }

    @Override
    public SocketPart createSocketPart(ClientThreadManager clientThread) {
        return new InteractiveSocketPart(clientThread);
    }

    @Override
    public GoThroughSocketChannel<InteractiveModel, InteractiveModel> createGoThroughSocketChannel(String ip, Integer port) {
        try {
            Socket socket = SocketUtils.createSocket(ip, port);
            return new InteractiveChannel(socket);
        } catch (IOException e) {
            log.error("[{}:{}]创建Socket失败", ip, port, e);
            return null;
        }
    }
}
