package com.future94.gothrough.server.listen.handler;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;

/**
 * @author weilai
 */
public class HeartBeatHandler implements ClientRecvHandler<InteractiveModel, InteractiveModel> {

    private static final HeartBeatHandler INSTANCE = new HeartBeatHandler();

    private HeartBeatHandler() {

    }

    public static HeartBeatHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean process(InteractiveModel model, GoThroughSocketChannel<InteractiveModel, InteractiveModel> channel) throws Exception {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum.getEnumByName(model.getInteractiveType());
        if (!InteractiveTypeEnum.HEART_BEAT.equals(interactiveTypeEnum)) {
            return false;
        }
        InteractiveModel sendModel = InteractiveModel.of(model.getInteractiveSeq(), InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildSuccess());
        channel.writeAndFlush(sendModel);
        return true;
    }
}
