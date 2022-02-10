package com.future94.gothrough.client.handler;

import com.future94.gothrough.client.cache.ClientThreadCache;
import com.future94.gothrough.client.thread.ClientThreadManager;
import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.SequenceUtils;
import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ClientConnectDTO;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.protocol.nio.handler.SimpleChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;
import com.future94.gothrough.protocol.part.InteractiveSocketPart;
import com.future94.gothrough.protocol.part.SocketPart;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 处理{@link InteractiveTypeEnum#CLIENT_CONNECT_ANSWER}消息
 *
 * @author weilai
 */
@Slf4j
public class ClientConnectAnswerReadableHandler extends SimpleChannelReadableHandler<InteractiveModel> {

    @Override
    protected boolean support(InteractiveModel msg) {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum
                .getEnumByName(msg.getInteractiveType());
        return InteractiveTypeEnum.CLIENT_CONNECT_ANSWER.equals(interactiveTypeEnum);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InteractiveModel msg) {
        InteractiveResultDTO resultDTO = msg.getData().convert(InteractiveResultDTO.class);
        if (resultDTO.isSuccess()) {
            log.info("建立连接端口服务端回复成功");
            ClientConnectDTO clientDTO = resultDTO.covert(ClientConnectDTO.class);
            Integer listenPort = SequenceUtils.getSocketPortByPartKey(clientDTO.getSocketPartKey());
            ClientThreadManager clientThreadManager = ClientThreadCache.get(listenPort);
            SocketChannel exposedSocketChannel = clientThreadManager.getExposedSocketChannel();
            if (exposedSocketChannel == null || !exposedSocketChannel.isConnected()) {
                log.error("与要暴漏端建立SocketChannel失败");
                return;
            }
            final String serverIp = clientThreadManager.getConfig().getServerIp();
            final Integer serverPort = clientThreadManager.getConfig().getServerPort();
            SocketChannel serverSocketChannel;
            try {
                serverSocketChannel = SocketUtils.createBlockSocketChannel(serverIp, serverPort);
            } catch (IOException e) {
                log.error("向中转服务[{}:{}]建立连接失败", serverIp, serverPort);
                return;
            }
            SocketPart socketPart = new InteractiveSocketPart(clientThreadManager);
            socketPart.setSocketPartKey(clientDTO.getSocketPartKey());
            socketPart.setSendSocket(serverSocketChannel);
            socketPart.setRecvSocket(exposedSocketChannel);
            boolean passWayStatus = socketPart.createPassWay();
            if (!passWayStatus) {
                log.error("尝试打通隧道失败, socketPartKey:[{}]", clientDTO.getSocketPartKey());
                socketPart.cancel();
                return;
            }
            clientThreadManager.addSocketPartCache(clientDTO.getSocketPartKey(), socketPart);
        } else {
            log.warn("建立连接端口服务端回复失败");
        }
    }
}
