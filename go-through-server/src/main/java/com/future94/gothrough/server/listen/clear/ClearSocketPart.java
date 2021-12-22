package com.future94.gothrough.server.listen.clear;

/**
 * @author weilai
 */
public interface ClearSocketPart extends Runnable {

    void start();

    void cancel();

}
