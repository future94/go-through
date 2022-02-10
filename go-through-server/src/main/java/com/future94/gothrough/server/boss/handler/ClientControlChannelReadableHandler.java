package com.future94.gothrough.server.boss.handler;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ClientControlDTO;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.protocol.nio.handler.SimpleChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;
import com.future94.gothrough.server.cache.GoThroughContextHolder;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 处理{@link InteractiveTypeEnum#CLIENT_CONTROL}消息
 * 响应{@link InteractiveTypeEnum#CLIENT_CONTROL_ANSWER}消息
 *
 * @author weilai
 */
@Slf4j
public class ClientControlChannelReadableHandler extends SimpleChannelReadableHandler<InteractiveModel> {

    @Override
    protected boolean support(InteractiveModel msg) {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum.getEnumByName(msg.getInteractiveType());
        return InteractiveTypeEnum.CLIENT_CONTROL.equals(interactiveTypeEnum);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InteractiveModel msg) {
        ClientControlDTO clientControlModel = msg.getData().convert(ClientControlDTO.class);
        final Integer serverExposedListenPort = clientControlModel.getServerExposedListenPort();
        ServerListenThreadManager serverListenThreadManager = GoThroughContextHolder.getServerListenThreadManager(serverExposedListenPort);
        if (serverListenThreadManager == null) {
            ctx.write(InteractiveModel.of(msg.getInteractiveSeq(), InteractiveTypeEnum.CLIENT_CONTROL_ANSWER, InteractiveResultDTO.buildNoServerListen()));
            return;
        }
        GoThroughContextHolder.setClientControlSocketChannel(serverExposedListenPort, ctx.getSocketChannel());
        try {
            serverListenThreadManager.start();
            ctx.write(InteractiveModel.of(msg.getInteractiveSeq(), InteractiveTypeEnum.CLIENT_CONTROL_ANSWER, InteractiveResultDTO.buildSuccess(clientControlModel)));
        } catch (IOException e) {
            log.error("serverListenThreadManager start error, port:[{}]", serverListenThreadManager.getListenPort());
            ctx.write(InteractiveModel.of(msg.getInteractiveSeq(), InteractiveTypeEnum.CLIENT_CONTROL_ANSWER, InteractiveResultDTO.buildNoServerListen()));
        }
    }
}
