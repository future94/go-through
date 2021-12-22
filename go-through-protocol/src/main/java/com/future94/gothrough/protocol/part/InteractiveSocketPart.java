package com.future94.gothrough.protocol.part;

import com.future94.gothrough.protocol.passway.InteractivePassWay;
import com.future94.gothrough.protocol.thread.ThreadManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author weilai
 */
@Slf4j
public class InteractiveSocketPart extends BaseSocketPart implements ThreadManager {

    private InteractivePassWay outToInPassWay;

    private InteractivePassWay inToOutPassWay;

    private Boolean nio;

    private final CountDownLatch countDownLatch = new CountDownLatch(2);

    public InteractiveSocketPart(ThreadManager threadManager) {
        super(threadManager);
        nio = threadManager.getNio();
    }

    @Override
    public Boolean getNio() {
        return nio;
    }

    @Override
    public void cancel() {
        if (this.isCancel) {
            return;
        }
        this.isCancel = true;
        this.isAlive = false;
        log.debug("InteractiveSocketPart {} will cancel", this.socketPartKey);
        if (this.outToInPassWay != null) {
            this.outToInPassWay.cancel();
            this.outToInPassWay = null;
        }
        if (this.inToOutPassWay != null) {
            this.inToOutPassWay.cancel();
            this.inToOutPassWay = null;
        }
        if (this.recvSocket != null) {
            try {
                this.recvSocket.close();
            } catch (IOException e) {
                log.warn("InteractiveSocketPart [{}] 监听端口 关闭异常", this.socketPartKey);
            }
            this.recvSocket = null;
        }
        if (this.sendSocket != null) {
            try {
                this.sendSocket.close();
            } catch (IOException e) {
                log.warn("InteractiveSocketPart [{}] 发送端口 关闭异常", this.socketPartKey);
            }
            this.sendSocket = null;
        }
        log.debug("InteractiveSocketPart [{}] is cancelled", this.socketPartKey);
    }

    @Override
    public boolean createPassWay() {
        if (this.isCancel) {
            throw new IllegalStateException("不得重启已退出的InteractiveSocketPart");
        }
        if (this.isAlive) {
            return true;
        }
        this.isAlive = true;

        try {
            this.outToInPassWay = new InteractivePassWay(this, this.recvSocket, this.sendSocket, getStreamCacheSize());
            this.inToOutPassWay = new InteractivePassWay(this, this.sendSocket, this.recvSocket, getStreamCacheSize());
            this.outToInPassWay.start();
            this.inToOutPassWay.start();
        } catch (Exception e) {
            log.error("socketPart [" + this.socketPartKey + "] 隧道建立异常", e);
            this.exit();
            return false;
        }
        return true;
    }

    private void exit() {
        this.cancel();
        if (this.threadManager != null) {
            threadManager.stopSocketPart(this.socketPartKey);
            threadManager = null;
        }
    }

    @Override
    public void noticeStopPassWay() {
        this.countDownLatch.countDown();
        if (this.countDownLatch.getCount() <= 0) {
            this.exit();
        }
    }
}
