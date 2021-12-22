package com.future94.gothrough.server.listen.service;

import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.part.SocketPart;
import com.future94.gothrough.server.listen.clear.ClearInvalidSocketPartThread;
import com.future94.gothrough.server.listen.manager.SocketManager;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;

import java.net.Socket;

/**
 * @author weilai
 */
public interface ListenServerService {

    SocketPart createSocketPart(ServerListenThreadManager serverListenThreadManager);

    ClearInvalidSocketPartThread createClearInvalidSocketPartThread(ServerListenThreadManager serverListenThreadManager);

    SocketManager createSocketManager(Socket socket);

    GoThroughSocketChannel<InteractiveModel, InteractiveModel> createGoThroughSocketChannel(Socket socket);
}
