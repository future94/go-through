package com.future94.gothrough.protocol.thread;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @author weilai
 */
public abstract class AbstractNIORunnable implements GoThroughNIORunnable {

    protected abstract void doProcess(SelectionKey key);

    @Override
    public void process(SelectionKey key, int interestOps, SelectableChannel channel) {
        doProcess(key);
        if (!GoThroughNioContainer.reRegisterByKey(key, interestOps)) {
            GoThroughNioContainer.release(channel);
        }
    }
}
