package com.future94.gothrough.protocol.part;

import com.future94.gothrough.protocol.thread.ThreadManager;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author weilai
 */
@Data
public abstract class BaseSocketPart implements SocketPart {

    protected volatile boolean isAlive = false;

    protected volatile boolean isCancel = false;

    protected String socketPartKey;

    protected SocketChannel recvSocket;

    protected SocketChannel sendSocket;

    private final LocalDateTime createTime;

    /**
     * 等待连接有效时间，ms
     */
    @Getter
    @Setter
    protected long validMillis = 60000L;

    @Getter
    @Setter
    private int streamCacheSize = 1 << 12;

    protected ThreadManager threadManager;

    public BaseSocketPart(ThreadManager threadManager) {
        this.threadManager = threadManager;
        this.createTime = LocalDateTime.now();
    }

    @Override
    public boolean isValid() {
        if (this.isCancel) {
            return false;
        }

        if (this.isAlive) {
            return true;
        }

        long millis = Duration.between(this.createTime, LocalDateTime.now()).toMillis();
        return millis < this.validMillis;
    }
}
