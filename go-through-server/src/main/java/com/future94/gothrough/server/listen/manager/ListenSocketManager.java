package com.future94.gothrough.server.listen.manager;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.protocol.model.dto.ServerWaitClientDTO;
import com.future94.gothrough.server.listen.handler.ClientRecvHandler;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author weilai
 */
@Slf4j
public class ListenSocketManager implements SocketManager {

    private AtomicBoolean isStart = new AtomicBoolean(false);

    private AtomicBoolean isCancel = new AtomicBoolean(false);

    private RecvThread recvThread;

    private ServerListenThreadManager serverListenThreadManager;

    private List<ClientRecvHandler<InteractiveModel, InteractiveModel>> recvPipelineList = new LinkedList<>();

    protected final GoThroughSocketChannel<InteractiveModel, InteractiveModel> goThroughSocketChannel;

    public ListenSocketManager(GoThroughSocketChannel<InteractiveModel, InteractiveModel> goThroughSocketChannel) {
        this.goThroughSocketChannel = goThroughSocketChannel;
    }

    @Override
    public boolean isValid() {
        Socket socket = (this.goThroughSocketChannel == null) ? null : this.goThroughSocketChannel.getSocket();
        boolean closeFlag = (socket == null) || (!socket.isConnected() || socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown());
        if (closeFlag) {
            return false;
        }
        try {
            // 心跳测试
            InteractiveModel interactiveModel = InteractiveModel.of(InteractiveTypeEnum.HEART_BEAT, null);
            goThroughSocketChannel.writeAndFlush(interactiveModel);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean sendServerWaitClient(String socketPartKey) {
        InteractiveModel model = InteractiveModel.of(InteractiveTypeEnum.SERVER_WAIT_CLIENT, new ServerWaitClientDTO(socketPartKey));
        try {
            this.goThroughSocketChannel.writeAndFlush(model);
        } catch (Throwable e) {
            return false;
        }

        return true;
    }

    @Override
    public void startRecv() {
        if (!this.isStart.compareAndSet(false, true)) {
            log.warn("已经启动接收了");
            return;
        }

        if (this.recvThread == null || !this.recvThread.isAlive()) {
            this.recvThread = new RecvThread("control-recv-" + serverListenThreadManager.getListenPort());
            this.recvThread.start();
        }
    }

    @Override
    public void addRecvHandler(ClientRecvHandler<InteractiveModel, InteractiveModel> recvHandler) {
        this.recvPipelineList.add(recvHandler);
    }

    @Override
    public void setServerListenThreadManager(ServerListenThreadManager serverListenThreadManager) {
        this.serverListenThreadManager = serverListenThreadManager;
    }

    @Override
    public void close() throws IOException {
        if (!this.isCancel.compareAndSet(false, true)) {
            log.warn("已经关闭过了");
            return;
        }

        if (this.recvThread != null) {
            this.recvThread.interrupt();
            this.recvThread = null;
        }

        if (this.goThroughSocketChannel != null) {
            try {
                this.goThroughSocketChannel.close();
            } catch (IOException e) {
                // do no thing
                log.warn("关闭socketChannel异常", e);
            }
        }

        if (Objects.nonNull(this.serverListenThreadManager)) {
            this.serverListenThreadManager.controlCloseNotice(this);
            this.serverListenThreadManager = null;
        }
    }

    class RecvThread extends Thread {

        public RecvThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (isStart.get() && !isCancel.get()) {
                try {
                    InteractiveModel interactiveModel = goThroughSocketChannel.read();

                    log.info("监听线程 [{}] 接收到控制端口发来的消息：[ {} ]", serverListenThreadManager.getListenPort(), interactiveModel);
                    boolean procResult = false;
                    for (ClientRecvHandler<InteractiveModel, InteractiveModel> handler : recvPipelineList) {
                        procResult = handler.process(interactiveModel, goThroughSocketChannel);
                        if (procResult) {
                            break;
                        }
                    }

                    if (!procResult) {
                        log.warn("无处理方法的信息：[{}]", interactiveModel);

                        InteractiveModel result = InteractiveModel.of(interactiveModel.getInteractiveSeq(),
                                InteractiveTypeEnum.COMMON_REPLY,
                                InteractiveResultDTO.buildInteractiveTypeNotFound());
                        goThroughSocketChannel.writeAndFlush(result);
                    }

                } catch (Exception e) {
                    log.error("读取或写入异常", e);
                    if (e instanceof IOException || !isValid()) {
                        try {
                            close();
                        } catch (IOException ex) {
                            log.warn("关闭RecvThread异常", ex);
                        }
                    }
                }
            }
        }
    }
}
