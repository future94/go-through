package com.future94.gothrough.client.handler;

import com.future94.gothrough.client.adapter.ClientAdapter;
import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;

/**
 * @author weilai
 */
public class HeartBeatHandler implements ClientHandler<InteractiveModel, InteractiveModel> {

    private static final HeartBeatHandler INSTANCE = new HeartBeatHandler();

    private HeartBeatHandler() {
    }

    public static HeartBeatHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean process(InteractiveModel model, ClientAdapter<InteractiveModel, InteractiveModel> clientAdapter) throws Exception {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum.getEnumByName(model.getInteractiveType());
        if (!InteractiveTypeEnum.HEART_BEAT.equals(interactiveTypeEnum)) {
            return false;
        }
        InteractiveModel sendModel = InteractiveModel.of(model.getInteractiveSeq(), InteractiveTypeEnum.HEART_BEAT, InteractiveResultDTO.buildSuccess());
        clientAdapter.getGoThroughSocketChannel().writeAndFlush(sendModel);
        return true;
    }
}
