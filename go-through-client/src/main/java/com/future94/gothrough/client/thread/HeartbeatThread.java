package com.future94.gothrough.client.thread;

import com.future94.gothrough.protocol.thread.GoThroughThreadFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author weilai
 */
@Slf4j
public class HeartbeatThread implements Heartbeat, Runnable {

    private AtomicBoolean isAlive = new AtomicBoolean(false);

    private ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), GoThroughThreadFactory.create("heart-beat"), new ThreadPoolExecutor.CallerRunsPolicy());

    @Setter
    @Getter
    private int heartIntervalSeconds = 10;
    @Setter
    @Getter
    private int maxRetryConnectCount = 10;

    private int failCount = 0;

    private final ClientThreadManager clientThread;

    private volatile ScheduledFuture<?> scheduledFuture;

    public HeartbeatThread(ClientThreadManager clientThread) {
        this.clientThread = clientThread;
    }

    @Override
    public boolean isAlive() {
        return isAlive.get();
    }

    @Override
    public void cancel() {
        if (!this.isAlive.compareAndSet(true, false)) {
            log.warn("已经停止过了");
            return;
        }
        ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (Objects.nonNull(scheduledFuture) && !scheduledFuture.isCancelled()) {
            this.scheduledFuture = null;
            scheduledFuture.cancel(false);
        }
    }

    @Override
    public void start() {
        if (!isAlive.compareAndSet(false, true)) {
            log.warn("心跳已经启动过了");
            return;
        }
        ScheduledFuture<?> scheduledFuture = this.scheduledFuture;
        if (Objects.isNull(scheduledFuture) || scheduledFuture.isCancelled()) {
            this.failCount = 0;
            this.scheduledFuture = scheduledExecutor.scheduleWithFixedDelay(this, this.heartIntervalSeconds, this.heartIntervalSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void run() {
        if (this.clientThread.isCancel() || !this.isAlive()) {
            this.cancel();
        }
        log.debug("开始发送心跳数据到[{}:{}]", this.clientThread.getServerIp(), this.clientThread.getServerExposedListenPort());
        try {
            this.clientThread.sendHeartbeatTest();
            this.failCount = 0;
            return;
        } catch (Exception e) {
            log.warn("发送心跳数据到[{}:{}]异常", this.clientThread.getServerIp(), this.clientThread.getServerExposedListenPort());
            // FIXME: 发送心跳数据异常调用cannel后不会重试
            this.clientThread.cancel();
        }
        if (!this.isAlive.get()) {
            log.warn("发送心跳数据异常，但是心跳线程已经停止");
            return;
        }
        this.failCount++;
        try {
            boolean start = this.clientThread.start();
            if (start) {
                log.info("第{}次重新建立连接[{}:{}]成功", this.failCount, this.clientThread.getServerIp(), this.clientThread.getServerExposedListenPort());
                this.failCount = 0;
                return;
            } else {
                log.warn("第[{}]次重新建立连接[{}:{}]失败", this.failCount, this.clientThread.getServerIp(), this.clientThread.getServerExposedListenPort());
            }
        } catch (Exception ex) {
            log.warn("第[{}]次重新建立连接[{}:{}]失败", this.failCount, this.clientThread.getServerIp(), this.clientThread.getServerExposedListenPort(), ex);
        }

        if (this.failCount >= this.maxRetryConnectCount) {
            log.error("尝试重新连接[{}:{}]超过最大次数[{}]，关闭客户端", this.clientThread.getServerIp(), this.clientThread.getServerExposedListenPort(), this.getMaxRetryConnectCount());
            this.clientThread.cancel();
            this.cancel();
        }
    }
}
