package com.future94.gothrough.server.listen.manager;

import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.server.listen.handler.ClientRecvHandler;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;

import java.io.Closeable;

/**
 * @author weilai
 */
public interface SocketManager extends Closeable {

    boolean isValid();

    boolean sendServerWaitClient(String socketPartKey);

    void startRecv();

    void addRecvHandler(ClientRecvHandler<InteractiveModel, InteractiveModel> recvHandler);

    void setServerListenThreadManager(ServerListenThreadManager serverListenThreadManager);
}
