package com.future94.gothrough.client.thread;

import com.future94.gothrough.client.cache.ClientThreadCache;
import com.future94.gothrough.client.config.ClientConfig;
import com.future94.gothrough.client.handler.ClientConnectAnswerReadableHandler;
import com.future94.gothrough.client.handler.ClientControlAnswerReadableHandler;
import com.future94.gothrough.client.handler.CommonReplyChannelReadableHandler;
import com.future94.gothrough.client.handler.HeartBeatChannelReadableHandler;
import com.future94.gothrough.client.handler.ServerWaitClientChannelReadableHandler;
import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ClientConnectDTO;
import com.future94.gothrough.protocol.model.dto.ClientControlDTO;
import com.future94.gothrough.protocol.model.dto.ServerWaitClientDTO;
import com.future94.gothrough.protocol.nio.thread.client.GoThroughNioClient;
import com.future94.gothrough.protocol.nio.thread.client.NioClient;
import com.future94.gothrough.protocol.part.SocketPart;
import com.future94.gothrough.protocol.thread.ThreadManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilai
 */
@Slf4j
public class ClientThreadManager implements ThreadManager {

    @Getter
    private volatile boolean isAlive = false;

    @Getter
    private volatile boolean isCancel = false;

    @Getter
    private ClientConfig config;

    private Heartbeat heartbeatThread;

    /**
     * 与Server交互
     */
    private final NioClient client;

    /**
     * 与要暴露内部交互
     *
     * 调用{@link #createConnect(ServerWaitClientDTO)}方法时创建
     */
    @Getter
    private SocketChannel exposedSocketChannel;

    private final Map<String, SocketPart> socketPartCache = new ConcurrentHashMap<>();

    public ClientThreadManager(ClientConfig config) {
        this.config = config;
        GoThroughNioClient nioClient = new GoThroughNioClient();
        nioClient.setPort(config.getServerPort());
        nioClient.setReadableHandler(new CommonReplyChannelReadableHandler());
        nioClient.setReadableHandler(new HeartBeatChannelReadableHandler());
        nioClient.setReadableHandler(new ClientControlAnswerReadableHandler());
        nioClient.setReadableHandler(new ServerWaitClientChannelReadableHandler());
        nioClient.setReadableHandler(new ClientConnectAnswerReadableHandler());
        nioClient.setIp(config.getServerIp());
        nioClient.setPort(config.getServerPort());
        this.client = nioClient;
        ClientThreadCache.add(this);
    }

    public boolean start() throws Exception {
        client.connect();
        boolean flag = createControlChannel();
        if (!flag) {
            return false;
        }
        this.start0();
        return true;
    }

    private void start0() {
        if (this.isAlive) {
            log.warn("已经启动过了");
            return;
        }
//        if (Objects.isNull(this.heartbeatThread) || !this.heartbeatThread.isAlive()) {
//            HeartbeatThread heartbeatThread = new HeartbeatThread(this);
//            heartbeatThread.setHeartIntervalSeconds(10);
//            heartbeatThread.setMaxRetryConnectCount(10);
//            heartbeatThread.start();
//            this.heartbeatThread = heartbeatThread;
//        }
        this.isAlive = true;
    }

    public void cancel() {
        if (this.isCancel) {
            log.warn("已经取消过了");
            return;
        }

        if (this.heartbeatThread != null) {
            this.heartbeatThread.cancel();
            this.heartbeatThread = null;
        }

        Iterator<SocketPart> iterator = this.socketPartCache.values().iterator();
        if (iterator.hasNext()) {
            SocketPart socketPart = iterator.next();
            iterator.remove();
            socketPart.cancel();
        }
        this.socketPartCache.clear();
        this.isCancel = true;
    }

    public void sendHeartbeatTest() throws Exception {
        client.writeChannel(InteractiveModel.of(InteractiveTypeEnum.HEART_BEAT, null));
    }

    private boolean createControlChannel() {
        String serverIp = this.config.getServerIp();
        Integer serverPort = this.config.getServerPort();
        Integer serverExposedListenPort = this.config.getServerExposedListenPort();
        try {
            return client.writeChannel(InteractiveModel.of(InteractiveTypeEnum.CLIENT_CONTROL,
                    ClientControlDTO.builder().serverExposedListenPort(serverExposedListenPort).build()));
        } catch (Exception e) {
            log.error("向服务端[{}:{}]发送CLIENT_CONTROL消息失败", serverIp, serverPort, e);
            return false;
        }
    }

    public boolean createConnect(ServerWaitClientDTO dto) {
        final String exposedIntranetIp = this.getConfig().getExposedIntranetIp();
        final Integer exposedIntranetPort = this.getConfig().getExposedIntranetPort();
        try {
            this.exposedSocketChannel = SocketUtils.createBlockSocketChannel(exposedIntranetIp, exposedIntranetPort);
        } catch (IOException e) {
            log.error("向暴露目标[{}:{}]建立连接失败", exposedIntranetIp, exposedIntranetPort);
            return false;
        }
        final String serverIp = this.config.getServerIp();
        final Integer serverPort = this.config.getServerPort();
        try {
            // 向服务端请求建立隧道
            client.writeChannel(InteractiveModel.of(InteractiveTypeEnum.CLIENT_CONNECT, ClientConnectDTO.builder().socketPartKey(dto.getSocketPartKey()).build()));
            return true;
        } catch (Exception e) {
            log.error("向服务端[{}:{}]发送CLIENT_CONNECT消息失败", serverIp, serverPort, e);
            return false;
        }
    }

    public void addSocketPartCache(String socketPartKey, SocketPart socketPart) {
        this.socketPartCache.put(socketPartKey, socketPart);
    }
}
