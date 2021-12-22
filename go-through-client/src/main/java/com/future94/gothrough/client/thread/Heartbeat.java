package com.future94.gothrough.client.thread;

/**
 * @author weilai
 */
public interface Heartbeat {

    boolean isAlive();

    void cancel();

    void start();
}
