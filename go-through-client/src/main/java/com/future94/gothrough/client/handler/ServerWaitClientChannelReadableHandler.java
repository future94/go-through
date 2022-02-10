package com.future94.gothrough.client.handler;

import com.future94.gothrough.client.cache.ClientThreadCache;
import com.future94.gothrough.client.thread.ClientThreadManager;
import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.SequenceUtils;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.protocol.model.dto.ServerWaitClientDTO;
import com.future94.gothrough.protocol.nio.handler.SimpleChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;

/**
 * 处理{@link InteractiveTypeEnum#SERVER_WAIT_CLIENT}消息
 *
 * @author weilai
 */
public class ServerWaitClientChannelReadableHandler extends SimpleChannelReadableHandler<InteractiveModel> {

    @Override
    protected boolean support(InteractiveModel msg) {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum
                .getEnumByName(msg.getInteractiveType());
        return InteractiveTypeEnum.SERVER_WAIT_CLIENT.equals(interactiveTypeEnum);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InteractiveModel msg) {
        ServerWaitClientDTO clientDTO = msg.getData().convert(ServerWaitClientDTO.class);
        Integer listenPort = SequenceUtils.getSocketPortByPartKey(clientDTO.getSocketPartKey());
        ClientThreadManager clientThreadManager = ClientThreadCache.get(listenPort);
        if (clientThreadManager == null) {
            ctx.write(InteractiveModel.of(msg.getInteractiveSeq(),
                    InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildNoServerListen()));
            return;
        }
        clientThreadManager.createConnect(clientDTO);
    }
}
