package com.future94.gothrough.server.process;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.SequenceUtils;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ClientConnectDTO;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.server.listen.cache.ServerListenThreadCache;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;

public class ClientConnectProcess implements Process {

    private static final ClientConnectProcess INSTANCE = new ClientConnectProcess();

    private ClientConnectProcess() {

    }

    public static ClientConnectProcess getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isProcess(InteractiveModel recvInteractiveModel) {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum
                .getEnumByName(recvInteractiveModel.getInteractiveType());
        return InteractiveTypeEnum.CLIENT_CONNECT.equals(interactiveTypeEnum);
    }

    @Override
    public boolean run(GoThroughSocketChannel<InteractiveModel, InteractiveModel> goThroughSocketChannel,
                       InteractiveModel recvInteractiveModel) throws Exception {
        ClientConnectDTO clientConnectModel = recvInteractiveModel.getData().convert(ClientConnectDTO.class);
        Integer listenPort = SequenceUtils.getSocketPortByPartKey(clientConnectModel.getSocketPartKey());

        ServerListenThreadManager serverListenThreadManager = ServerListenThreadCache.get(listenPort);

        if (serverListenThreadManager == null) {
            goThroughSocketChannel.writeAndFlush(InteractiveModel.of(recvInteractiveModel.getInteractiveSeq(),
                    InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildNoServerListen()));
            return false;
        }

        // 回复设置成功，如果doSetPartClient没有找到对应的搭档，则直接按关闭处理
        goThroughSocketChannel.writeAndFlush(InteractiveModel.of(recvInteractiveModel.getInteractiveSeq(),
                InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildSuccess()));

        // 若设置成功，则上层无需关闭
        // 若设置失败，则由上层关闭
        return serverListenThreadManager.doSetPartClient(clientConnectModel.getSocketPartKey(),
                goThroughSocketChannel.getSocket());
    }

}
