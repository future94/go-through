package com.future94.gothrough.client.thread;

import com.future94.gothrough.client.adapter.ClientAdapter;
import com.future94.gothrough.client.config.ClientConfig;
import com.future94.gothrough.client.service.ClientService;
import com.future94.gothrough.client.service.impl.InteractiveClientService;
import com.future94.gothrough.protocol.part.SocketPart;
import com.future94.gothrough.protocol.thread.ThreadManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author weilai
 */
@Slf4j
public class ClientThreadManager implements ThreadManager {

    private AtomicBoolean isAlive = new AtomicBoolean(false);

    private AtomicBoolean isCancel = new AtomicBoolean(false);

    private volatile Heartbeat heartbeatThread;

    private volatile WaiMessageThread waiMessageThread;

    private volatile ClientConfig config;

    private volatile ClientService<?, ?> clientService;

    private volatile ClientAdapter<?, ?> clientAdapter;

    private final Map<String, SocketPart> socketPartMap = new ConcurrentHashMap<>();

    public ClientThreadManager(ClientConfig config) {
        this.config = config;
    }

    public boolean start() throws Exception {
        this.clientService = new InteractiveClientService();

        if (this.clientAdapter == null) {
            this.clientAdapter = this.clientService.createControlAdapter(this);
        }

        boolean flag = this.clientAdapter.createControlChannel();

        if (!flag) {
            return false;
        }
        this.start0();
        return true;
    }

    private void start0() {
        if (!this.isAlive.compareAndSet(false, true)) {
            log.warn("已经启动过了");
            return;
        }

        Heartbeat heartbeatThread = this.heartbeatThread;
        if (Objects.isNull(heartbeatThread) || !heartbeatThread.isAlive()) {
            heartbeatThread = this.heartbeatThread = this.clientService.createHeartbeatThread(this);
            if (Objects.nonNull(heartbeatThread)) {
                heartbeatThread.start();
            }
        }

        if (Objects.isNull(this.waiMessageThread) || !this.waiMessageThread.isAlive()) {
            this.waiMessageThread = new WaiMessageThread();
            this.waiMessageThread.start();
        }
    }

    public void stopClient() {
        if (!this.isAlive.compareAndSet(true, false)) {
            log.warn("已经停止过了");
            return;
        }
        WaiMessageThread waiMessageThread = this.waiMessageThread;
        if (waiMessageThread != null) {
            this.waiMessageThread = null;
            waiMessageThread.interrupt();
        }
        ClientAdapter<?, ?> clientAdapter = this.clientAdapter;
        if (Objects.nonNull(clientAdapter)) {
            try {
                clientAdapter.close();
            } catch (Exception e) {
                log.error("client adapter close error", e);
            }
        }
    }

    public void sendHeartbeatTest() throws Exception {
        this.clientAdapter.sendHeartbeatTest();
    }

    public boolean isCancelled() {
        return isCancel.get();
    }

    public String getServerIp() {
        return this.config.getServerIp();
    }

    public Integer getServerPort() {
        return this.config.getServerPort();
    }

    @Override
    public Boolean getNio() {
        return this.config.getNio();
    }

    public Integer getServerExposedListenPort() {
        return this.config.getServerExposedListenPort();
    }

    public String getExposedIntranetIp() {
        return this.config.getExposedIntranetIp();
    }

    public Integer getExposedIntranetPort() {
        return this.config.getExposedIntranetPort();
    }

    public void cancel() {
        if (!this.isCancel.compareAndSet(false, true)) {
            log.warn("已经取消过了");
            return;
        }

        this.stopClient();

        if (this.heartbeatThread != null) {
            this.heartbeatThread = null;
        }

        ClientAdapter<?, ?> clientAdapter;
        if ((clientAdapter = this.clientAdapter) != null) {
            this.clientAdapter = null;
            try {
                clientAdapter.close();
            } catch (Exception e) {
                // do no thing
            }
        }

        String[] array = this.socketPartMap.keySet().toArray(new String[0]);

        for (String key : array) {
            this.stopSocketPart(key);
        }
    }

    @Override
    public void stopSocketPart(String socketPartKey) {
        SocketPart socketPart = this.socketPartMap.remove(socketPartKey);
        if (socketPart == null) {
            return;
        }
        socketPart.cancel();
    }

    public void addSocketPart(String socketPartKey, SocketPart socketPart) {
        this.socketPartMap.put(socketPartKey, socketPart);
    }

    class WaiMessageThread extends Thread {

        @Override
        public void run() {
            while (isAlive.get()) {
                try {
                    clientAdapter.waitMessage();
                } catch (Exception e) {
                    log.warn("client control [{}] to server is exception,will stopClient",
                            config.getServerExposedListenPort());
                    stopClient();
                }
            }
        }

    }
}
