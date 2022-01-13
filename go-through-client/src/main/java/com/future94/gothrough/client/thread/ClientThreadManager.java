package com.future94.gothrough.client.thread;

import com.future94.gothrough.client.cache.ClientThreadCache;
import com.future94.gothrough.client.config.ClientConfig;
import com.future94.gothrough.client.handler.CommonReplyChannelReadableHandler;
import com.future94.gothrough.client.handler.HeartBeatChannelReadableHandler;
import com.future94.gothrough.client.handler.ServerWaitClientChannelReadableHandler;
import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.SocketUtils;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ClientControlDTO;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.protocol.model.dto.ServerWaitClientDTO;
import com.future94.gothrough.protocol.nio.thread.server.GoThroughNioServer;
import com.future94.gothrough.protocol.nio.thread.server.NioServer;
import com.future94.gothrough.protocol.part.InteractiveSocketPart;
import com.future94.gothrough.protocol.part.SocketPart;
import com.future94.gothrough.protocol.thread.ThreadManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
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

    private ClientConfig config;

    private Heartbeat heartbeatThread;

    private volatile SocketChannel serverSocketChannel;

    private final Object lock = new Object();

    private final NioServer server;

    private final Map<String, SocketPart> socketPartCache = new ConcurrentHashMap<>();

    public ClientThreadManager(ClientConfig config) {
        this.config = config;
        GoThroughNioServer nioServer = new GoThroughNioServer();
        nioServer.setPort(config.getServerPort());
        nioServer.setReadableHandler(new CommonReplyChannelReadableHandler());
        nioServer.setReadableHandler(new HeartBeatChannelReadableHandler());
        nioServer.setReadableHandler(new ServerWaitClientChannelReadableHandler());
        this.server = nioServer;
        ClientThreadCache.add(this);
    }

    public boolean start() throws Exception {
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
        if (Objects.isNull(this.heartbeatThread) || !this.heartbeatThread.isAlive()) {
            HeartbeatThread heartbeatThread = new HeartbeatThread(this);
            heartbeatThread.setHeartIntervalSeconds(10);
            heartbeatThread.setMaxRetryConnectCount(10);
            heartbeatThread.start();
            this.heartbeatThread = heartbeatThread;
        }
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
        server.writeChannel(getServerSocketChannel(), InteractiveModel.of(InteractiveTypeEnum.HEART_BEAT, null));
    }

    public SocketChannel getServerSocketChannel() throws IOException{
        if (this.serverSocketChannel == null) {
            synchronized (this.lock) {
                if (this.serverSocketChannel == null) {
                    String serverIp = this.config.getServerIp();
                    Integer serverPort = this.config.getServerPort();
                    try {
                        this.serverSocketChannel = SocketUtils.createSocketChannel(serverIp, serverPort, true);
                    } catch (IOException e) {
                        log.error("向服务端[{}:{}]建立控制通道失败", serverIp, serverPort, e);
                        throw e;
                    }
                }
            }
        }
        return this.serverSocketChannel;
    }

    private boolean createControlChannel() {
        String serverIp = this.config.getServerIp();
        Integer serverPort = this.config.getServerPort();
        Integer serverExposedListenPort = this.config.getServerExposedListenPort();
        try {
            server.writeChannel(getServerSocketChannel(), InteractiveModel.of(InteractiveTypeEnum.CLIENT_CONTROL,
                    ClientControlDTO.builder().serverExposedListenPort(serverExposedListenPort).build()));
        } catch (Exception e) {
            log.error("向服务端[{}:{}]发送CLIENT_CONTROL消息失败", serverIp, serverPort, e);
            try {
                getServerSocketChannel().close();
            } catch (IOException ex) {
                log.warn("向服务端[{}:{}]发送CLIENT_CONTROL消息失败, 关闭socketChannel发生异常", serverIp, serverPort, e);
            }
            return false;
        }
        try {
            InteractiveModel recv = (InteractiveModel) server.readBlockSocketChannel(getServerSocketChannel());
            log.debug("建立控制端口回复：{}", recv);
            InteractiveResultDTO resultDTO = recv.getData().convert(InteractiveResultDTO.class);
            if (!resultDTO.isSuccess()) {
                log.error("服务端控制端口失败");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("处理服务端[{}:{}]发送建立控制端口回复消息失败", serverIp, serverPort, e);
            try {
                getServerSocketChannel().close();
            } catch (IOException ex) {
                log.warn("处理服务端[{}:{}]发送建立控制端口回复消息失败, 关闭socketChannel发生异常", serverIp, serverPort, e);
            }
            return false;
        }
    }

    public boolean createConnect(ServerWaitClientDTO dto) {
        String serverIp = this.config.getServerIp();
        Integer serverPort = this.config.getServerPort();
        String exposedIntranetIp = this.config.getExposedIntranetIp();
        Integer exposedIntranetPort = this.config.getExposedIntranetPort();
        // 首先向暴露目标建立socket
        SocketChannel exposedSocketChannel;
        try {
            exposedSocketChannel = SocketUtils.createSocketChannel(exposedIntranetIp, exposedIntranetPort, false);
        } catch (IOException e) {
            log.error("向暴露目标[{}:{}]建立连接失败", exposedIntranetIp, exposedIntranetPort);
            return false;
        }
        try {
            // 向服务端请求建立隧道
            server.writeChannel(getServerSocketChannel(), InteractiveModel.of(InteractiveTypeEnum.CLIENT_CONNECT, dto));
        } catch (Exception e) {
            log.error("向服务端[{}:{}]发送CLIENT_CONNECT消息失败", serverIp, serverPort, e);
            return false;
        }
        try {
            InteractiveModel recv = (InteractiveModel) server.readBlockSocketChannel(getServerSocketChannel());
            log.debug("建立隧道回复：{}", recv);
            InteractiveResultDTO resultDTO = recv.getData().convert(InteractiveResultDTO.class);
            if (!resultDTO.isSuccess()) {
                log.error("服务端绑定链接失败, 要暴露[{}:{}]", exposedIntranetIp, exposedIntranetPort);
                return false;
            }
        } catch (Exception e) {
            log.error("打通隧道[{}:{} <===> {}:{}]发生异常 ", serverIp, serverPort, exposedIntranetIp, exposedIntranetPort, e);
            try {
                exposedSocketChannel.close();
            } catch (IOException ex) {
                log.warn("关闭要暴露[{}:{}]的Socket发生异常", exposedIntranetIp, exposedIntranetPort, ex);
            }
            try {
                getServerSocketChannel().close();
            } catch (IOException ex) {
                log.warn("关闭要暴露[{}:{}]的SocketChannel发生异常", exposedIntranetIp, exposedIntranetPort, ex);
            }
            return false;
        }
        try {
            SocketPart socketPart = new InteractiveSocketPart(this);
            socketPart.setSocketPartKey(dto.getSocketPartKey());
            socketPart.setSendSocket(getServerSocketChannel());
            socketPart.setRecvSocket(exposedSocketChannel);
            boolean passWayStatus = socketPart.createPassWay();
            if (!passWayStatus) {
                log.error("尝试打通隧道失败, socketPartKey:[{}]", dto.getSocketPartKey());
                socketPart.cancel();
                return false;
            }
            this.socketPartCache.put(dto.getSocketPartKey(), socketPart);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Integer getServerExposedListenPort() {
        return this.config.getServerExposedListenPort();
    }

    public String getServerIp() {
        return this.config.getServerIp();
    }
}
