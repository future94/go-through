package com.future94.gothrough.protocol.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author weilai
 */
public class GoThroughThreadFactory implements ThreadFactory {

    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("goThrough");

    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);

    private String prefix;

    private int newPriority;

    private boolean daemon;

    private GoThroughThreadFactory() {
    }

    private GoThroughThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    public GoThroughThreadFactory(String prefix, int newPriority, boolean daemon) {
        this.prefix = prefix;
        this.newPriority = newPriority;
        this.daemon = daemon;
    }

    public static GoThroughThreadFactory create(String prefix) {
        return create(prefix, false, Thread.NORM_PRIORITY);
    }

    public static GoThroughThreadFactory create(String prefix, boolean daemon) {
        return create(prefix, daemon, Thread.NORM_PRIORITY);
    }

    public static GoThroughThreadFactory create(String prefix, boolean daemon, int newPriority) {
        return new GoThroughThreadFactory(prefix, newPriority, daemon);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(THREAD_GROUP, r, THREAD_GROUP.getName() + "-" + prefix + "-" + THREAD_NUMBER.getAndIncrement());
        thread.setDaemon(daemon);
        thread.setPriority(newPriority);
        return thread;
    }
}
