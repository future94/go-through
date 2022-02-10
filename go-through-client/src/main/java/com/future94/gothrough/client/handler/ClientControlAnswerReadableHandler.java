package com.future94.gothrough.client.handler;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.protocol.nio.handler.SimpleChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理{@link InteractiveTypeEnum#CLIENT_CONTROL_ANSWER}消息
 *
 * @author weilai
 */
@Slf4j
public class ClientControlAnswerReadableHandler extends SimpleChannelReadableHandler<InteractiveModel> {

    @Override
    protected boolean support(InteractiveModel msg) {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum
                .getEnumByName(msg.getInteractiveType());
        return InteractiveTypeEnum.CLIENT_CONTROL_ANSWER.equals(interactiveTypeEnum);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InteractiveModel msg) {
        InteractiveResultDTO resultDTO = msg.getData().convert(InteractiveResultDTO.class);
        if (resultDTO.isSuccess()) {
            log.info("建立控制端口服务端回复成功");
        } else {
            log.warn("建立控制端口服务端回复失败");
        }
    }
}
