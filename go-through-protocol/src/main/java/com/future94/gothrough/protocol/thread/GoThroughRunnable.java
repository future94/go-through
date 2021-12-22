package com.future94.gothrough.protocol.thread;

import java.nio.channels.SelectionKey;

/**
 * @author weilai
 */
public interface GoThroughRunnable {

    void process(SelectionKey key);
}
