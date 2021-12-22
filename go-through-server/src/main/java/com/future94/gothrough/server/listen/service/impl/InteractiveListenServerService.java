package com.future94.gothrough.server.listen.service.impl;

import com.future94.gothrough.protocol.channel.InteractiveChannel;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.part.InteractiveSocketPart;
import com.future94.gothrough.protocol.part.SocketPart;
import com.future94.gothrough.server.listen.clear.ClearInvalidSocketPartThread;
import com.future94.gothrough.server.listen.handler.CommonReplyHandler;
import com.future94.gothrough.server.listen.handler.HeartBeatHandler;
import com.future94.gothrough.server.listen.manager.ListenSocketManager;
import com.future94.gothrough.server.listen.manager.SocketManager;
import com.future94.gothrough.server.listen.service.ListenServerService;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

/**
 * @author weilai
 */
@Slf4j
public class InteractiveListenServerService implements ListenServerService {

    @Override
    public SocketPart createSocketPart(ServerListenThreadManager serverListenThreadManager) {
        return new InteractiveSocketPart(serverListenThreadManager);
    }

    @Override
    public ClearInvalidSocketPartThread createClearInvalidSocketPartThread(ServerListenThreadManager serverListenThreadManager) {
        return new ClearInvalidSocketPartThread(serverListenThreadManager);
    }

    @Override
    public SocketManager createSocketManager(Socket socket) {
        GoThroughSocketChannel<InteractiveModel, InteractiveModel> controlGoThroughSocketChannel = this
                .createGoThroughSocketChannel(socket);
        SocketManager socketManager = new ListenSocketManager(controlGoThroughSocketChannel);
        socketManager.addRecvHandler(CommonReplyHandler.getInstance());
        socketManager.addRecvHandler(HeartBeatHandler.getInstance());
        return socketManager;
    }

    @Override
    public GoThroughSocketChannel<InteractiveModel, InteractiveModel> createGoThroughSocketChannel(Socket socket) {
        InteractiveChannel interactiveChannel;
        try {
            interactiveChannel = new InteractiveChannel(socket);
        } catch (IOException e) {
            log.warn("创建InteractiveChannel失败", e);
            return null;
        }
        return interactiveChannel;
    }
}
