package com.future94.gothrough.protocol.thread;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @author weilai
 */
public interface GoThroughNIORunnable {

    void process(SelectionKey key, int interestOps, SelectableChannel channel);
}
