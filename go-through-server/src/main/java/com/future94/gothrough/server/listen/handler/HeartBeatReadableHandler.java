package com.future94.gothrough.server.listen.handler;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.protocol.nio.handler.SimpleChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;

/**
 * 处理{@link InteractiveTypeEnum#HEART_BEAT}消息
 * @author weilai
 */
public class HeartBeatReadableHandler extends SimpleChannelReadableHandler<InteractiveModel>{

    @Override
    protected boolean support(InteractiveModel msg) {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum
                .getEnumByName(msg.getInteractiveType());
        return InteractiveTypeEnum.HEART_BEAT.equals(interactiveTypeEnum);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InteractiveModel msg) {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum.getEnumByName(msg.getInteractiveType());
        if (!InteractiveTypeEnum.HEART_BEAT.equals(interactiveTypeEnum)) {
            return;
        }
        InteractiveModel sendModel = InteractiveModel.of(msg.getInteractiveSeq(), InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildSuccess());
        ctx.write(sendModel);
    }
}
