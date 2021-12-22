package com.future94.gothrough.server.process;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ClientControlDTO;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.server.listen.cache.ServerListenThreadCache;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;

/**
 * @author weilai
 */
public class ClientControlProcess implements Process {

    private static final ClientControlProcess INSTANCE = new ClientControlProcess();

    private ClientControlProcess() {

    }

    public static ClientControlProcess getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isProcess(InteractiveModel recvInteractiveModel) {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum
                .getEnumByName(recvInteractiveModel.getInteractiveType());
        return InteractiveTypeEnum.CLIENT_CONTROL.equals(interactiveTypeEnum);
    }

    @Override
    public boolean run(GoThroughSocketChannel<InteractiveModel, InteractiveModel> goThroughSocketChannel,
                       InteractiveModel recvInteractiveModel) throws Exception {
        ClientControlDTO clientControlModel = recvInteractiveModel.getData().convert(ClientControlDTO.class);
        ServerListenThreadManager serverListenThreadManager = ServerListenThreadCache.get(clientControlModel.getServerExposedListenPort());

        if (serverListenThreadManager == null) {
            goThroughSocketChannel.writeAndFlush(InteractiveModel.of(recvInteractiveModel.getInteractiveSeq(),
                    InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildNoServerListen()));
            return false;
        }

        goThroughSocketChannel.writeAndFlush(InteractiveModel.of(recvInteractiveModel.getInteractiveSeq(),
                InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildSuccess()));

        serverListenThreadManager.start(goThroughSocketChannel.getSocket());
        return true;
    }

}
