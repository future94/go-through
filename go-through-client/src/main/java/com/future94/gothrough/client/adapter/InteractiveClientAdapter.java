package com.future94.gothrough.client.adapter;

import com.future94.gothrough.client.handler.ClientHandler;
import com.future94.gothrough.client.service.ClientService;
import com.future94.gothrough.client.thread.ClientThreadManager;
import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ClientControlDTO;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.protocol.model.dto.ServerWaitClientDTO;
import com.future94.gothrough.protocol.part.SocketPart;
import com.future94.gothrough.protocol.thread.GoThroughThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author weilai
 */
@Slf4j
public class InteractiveClientAdapter implements ClientAdapter<InteractiveModel, InteractiveModel> {

    private final ClientThreadManager clientThread;

    private final ClientService<InteractiveModel, InteractiveModel> clientService;

    private GoThroughSocketChannel<InteractiveModel, InteractiveModel> goThroughSocketChannel;

    protected List<ClientHandler<InteractiveModel, InteractiveModel>> pipelineList = new LinkedList<>();

    private final ExecutorService executorService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), GoThroughThreadFactory.create("client-wait-message"), new ThreadPoolExecutor.CallerRunsPolicy());

    public InteractiveClientAdapter(ClientThreadManager clientThread, ClientService<InteractiveModel, InteractiveModel> clientService) {
        this.clientThread = clientThread;
        this.clientService = clientService;
    }

    public void setPipelineList(ClientHandler<InteractiveModel, InteractiveModel> pipeline) {
        this.pipelineList.add(pipeline);
    }

    @Override
    public GoThroughSocketChannel<InteractiveModel, InteractiveModel> getGoThroughSocketChannel() {
        return goThroughSocketChannel;
    }

    @Override
    public boolean createControlChannel() throws Exception {
        String serverIp = this.clientThread.getServerIp();
        Integer serverPort = this.clientThread.getServerPort();
        Integer serverExposedListenPort = this.clientThread.getServerExposedListenPort();
        GoThroughSocketChannel<InteractiveModel, InteractiveModel> goThroughSocketChannel = this.clientService.createGoThroughSocketChannel(serverIp, serverPort);
        if (goThroughSocketChannel == null) {
            log.error("向服务端[{}:{}]建立控制通道失败", serverIp, serverPort);
            return false;
        }
        InteractiveModel interactiveModel = InteractiveModel.of(InteractiveTypeEnum.CLIENT_CONTROL,
                ClientControlDTO.builder().serverExposedListenPort(serverExposedListenPort).build());
        goThroughSocketChannel.writeAndFlush(interactiveModel);
        InteractiveModel recv = goThroughSocketChannel.read();
        log.info("建立控制端口回复：{}", recv);
        InteractiveResultDTO resultDTO = recv.getData().convert(InteractiveResultDTO.class);
        if (resultDTO.isSuccess()) {
            // 使用相同的
            this.goThroughSocketChannel = goThroughSocketChannel;
            return true;
        }
        return false;
    }

    @Override
    public boolean createConnect(ServerWaitClientDTO dto) {
        // 首先向暴露目标建立socket
        Socket exposedSocket;
        String serverIp = this.clientThread.getServerIp();
        Integer serverPort = this.clientThread.getServerPort();
        String exposedIntranetIp = this.clientThread.getExposedIntranetIp();
        Integer exposedIntranetPort = this.clientThread.getExposedIntranetPort();
        try {
            exposedSocket = SocketUtils.createSocket(exposedIntranetIp, exposedIntranetPort);
        } catch (IOException e) {
            log.error("向目标[{}:{}]建立连接失败", exposedIntranetIp, exposedIntranetPort);
            return false;
        }
        GoThroughSocketChannel<InteractiveModel, InteractiveModel> passWayClientChannel = null;
        try {
            // 向服务端请求建立隧道
            passWayClientChannel = this.clientService.createGoThroughSocketChannel(serverIp, serverPort);
            InteractiveModel model = InteractiveModel.of(InteractiveTypeEnum.CLIENT_CONNECT, dto);
            passWayClientChannel.writeAndFlush(model);

            InteractiveModel recv = passWayClientChannel.read();
            log.info("建立隧道回复：{}", recv);
            InteractiveResultDTO resultDTO = recv.getData().convert(InteractiveResultDTO.class);
            if (!resultDTO.isSuccess()) {
                throw new RuntimeException("绑定失败");
            }
        } catch (Exception e) {
            log.error("打通隧道[{}:{} <===> {}:{}]发生异常 ", serverIp, serverPort, exposedIntranetIp, exposedIntranetPort, e);
            try {
                exposedSocket.close();
            } catch (IOException ex) {
                log.warn("关闭要暴露[{}:{}]的Socket发生异常", exposedIntranetIp, exposedIntranetPort, ex);
            }
            if (passWayClientChannel != null) {
                try {
                    passWayClientChannel.close();
                } catch (IOException ex) {
                    log.warn("关闭要暴露[{}:{}]的SocketChannel发生异常", exposedIntranetIp, exposedIntranetPort, ex);
                }
            }
            return false;
        }
        // 将两个socket建立伙伴关系
        SocketPart socketPart = this.clientService.createSocketPart(this.clientThread);
        socketPart.setSocketPartKey(dto.getSocketPartKey());
        socketPart.setSendSocket(passWayClientChannel.getSocket());
        socketPart.setRecvSocket(exposedSocket);
        // 尝试打通隧道
        boolean createPassWay = socketPart.createPassWay();
        if (!createPassWay) {
            socketPart.cancel();
            return false;
        }
        // 将socket伙伴放入客户端线程进行统一管理
        this.clientThread.addSocketPart(dto.getSocketPartKey(), socketPart);
        return true;
    }

    @Override
    public void waitMessage() throws Exception {
        InteractiveModel read = this.goThroughSocketChannel.read();
        executorService.execute(() -> processWaitMessage(read));
    }

    private void processWaitMessage(InteractiveModel interactiveModel) {
        log.info("客户端接收到新的消息: {}", interactiveModel);
        try {
            boolean interrupted = false;
            for (ClientHandler<InteractiveModel, InteractiveModel> handler : this.pipelineList) {
                interrupted = handler.process(interactiveModel, this);
                if (interrupted) {
                    break;
                }
            }
            if (!interrupted) {
                log.warn("未找到对应的pipeline处理消息[{}]", interactiveModel);
                InteractiveModel result = InteractiveModel.of(interactiveModel.getInteractiveSeq(), InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildInteractiveTypeNotFound());
                this.getGoThroughSocketChannel().writeAndFlush(result);
            }
        } catch (Exception e) {
            log.error("客户端处理接收到新的消息异常", e);
        }
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(this.goThroughSocketChannel)) {
            this.goThroughSocketChannel.close();
            this.goThroughSocketChannel = null;
        }
    }

    @Override
    public void sendHeartbeatTest() throws Exception {
        InteractiveModel interactiveModel = InteractiveModel.of(InteractiveTypeEnum.HEART_BEAT, null);
        this.goThroughSocketChannel.writeAndFlush(interactiveModel);
    }
}
