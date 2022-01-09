package com.future94.gothrough.server.handler;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.SequenceUtils;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ClientConnectDTO;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.protocol.nio.handler.SimpleChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;
import com.future94.gothrough.server.listen.cache.ServerListenThreadCache;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 处理{@link InteractiveTypeEnum#CLIENT_CONNECT}消息
 *
 * @author weilai
 */
@Slf4j
public class ClientConnectChannelReadableHandler extends SimpleChannelReadableHandler<InteractiveModel> {

    @Override
    protected boolean support(InteractiveModel msg) {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum
                .getEnumByName(msg.getInteractiveType());
        return InteractiveTypeEnum.CLIENT_CONNECT.equals(interactiveTypeEnum);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InteractiveModel msg) {
        ClientConnectDTO clientConnectModel = msg.getData().convert(ClientConnectDTO.class);
        Integer listenPort = SequenceUtils.getSocketPortByPartKey(clientConnectModel.getSocketPartKey());

        ServerListenThreadManager serverListenThreadManager = ServerListenThreadCache.get(listenPort);

        if (serverListenThreadManager == null) {
            ctx.write(InteractiveModel.of(msg.getInteractiveSeq(),
                    InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildNoServerListen()));
            return;
        }

        // 回复设置成功，如果doSetPartClient没有找到对应的搭档，则直接按关闭处理
        ctx.write(InteractiveModel.of(msg.getInteractiveSeq(),
                InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildSuccess()));

        // 若设置失败，则关闭
        boolean status = serverListenThreadManager.doSetPartClient(clientConnectModel.getSocketPartKey(), ctx.getSocketChannel());
        if (status) {
            try {
                ctx.getSocketChannel().close();
            } catch (IOException e) {
                log.error("socket channel close error", e);
            }
        }
    }
}
