package com.future94.gothrough.server.listen.handler;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.SequenceUtils;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ServerWaitClientDTO;
import com.future94.gothrough.protocol.nio.handler.AcceptHandler;
import com.future94.gothrough.protocol.part.InteractiveSocketPart;
import com.future94.gothrough.protocol.part.SocketPart;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author weilai
 */
@Slf4j
public class ClientWaitAcceptHandler implements AcceptHandler {

    private final ServerListenThreadManager manager;

    public ClientWaitAcceptHandler(ServerListenThreadManager manager) {
        this.manager = manager;
    }

    @Override
    public void accept(SelectionKey selectionKey) throws IOException {
        String socketPartKey = SequenceUtils.genSocketPartKey(this.manager.getListenPort());
        SocketPart socketPart = new InteractiveSocketPart(this.manager);
        socketPart.setSocketPartKey(socketPartKey);
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        socketPart.setRecvSocket(socketChannel);
        InteractiveModel model = InteractiveModel.of(InteractiveTypeEnum.SERVER_WAIT_CLIENT, new ServerWaitClientDTO(socketPartKey));
        try {
            // FIXME: 发送到与之建立链接的客户端SocketChannel
            this.manager.write(socketChannel, model);
            this.manager.setSocketPartCache(socketPartKey, socketPart);
        } catch (Exception e) {
            log.error("ClientWaitHandler write SERVER_WAIT_CLIENT message error", e);
            this.manager.stop();
        }
    }
}
