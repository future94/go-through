package com.future94.gothrough.server.listen.clear;

import com.future94.gothrough.protocol.thread.GoThroughThreadFactory;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
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
public class ClearInvalidSocketPartThread implements ClearSocketPart {

    private AtomicBoolean isAlive = new AtomicBoolean(false);

    private ScheduledFuture<?> scheduledFuture;

    private final ServerListenThreadManager serverListenThreadManager;

    @Getter
    @Setter
    private int clearIntervalSeconds = 100;

    private ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), GoThroughThreadFactory.create("clear-invalid-socket"), new ThreadPoolExecutor.CallerRunsPolicy());

    public ClearInvalidSocketPartThread(ServerListenThreadManager serverListenThreadManager) {
        this.serverListenThreadManager = serverListenThreadManager;
    }

    @Override
    public void run() {
        this.serverListenThreadManager.clearInvalidSocketPart();
    }

    @Override
    public void start() {
        if (!this.isAlive.compareAndSet(false, true)) {
            log.warn("已经启动过了");
            return;
        }
        if (Objects.isNull(this.scheduledFuture) || this.scheduledFuture.isCancelled()) {
            this.scheduledFuture = this.scheduledExecutor.scheduleWithFixedDelay(this, this.clearIntervalSeconds, this.clearIntervalSeconds, TimeUnit.SECONDS);
        }

        log.info("ClearInvalidSocketPartThread for [{}] started !", this.serverListenThreadManager.getListenPort());
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

        log.info("ClearInvalidSocketPartThread for [{}] cancell !", this.serverListenThreadManager.getListenPort());

    }
}
