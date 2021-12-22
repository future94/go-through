package com.future94.gothrough.common.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author weilai
 */
public class CountDownUpLatch {

    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        @Override
        protected int tryAcquireShared(int acquires) {
            if (acquires == 0) {
                return (getState() == 0) ? 0 : -1;
            }

            for (; ; ) {
                int c = getState();
                int nextc = c + acquires;
                if (compareAndSetState(c, nextc)) {
                    return acquires;
                }
            }
        }

        @Override
        protected boolean tryReleaseShared(int releases) {
            for (; ; ) {
                int c = getState();
                if (c == 0) {
                    return false;
                }
                int nextc = c - 1;
                if (compareAndSetState(c, nextc)) {
                    return nextc == 0;
                }
            }
        }
    }

    private final Sync sync;

    public CountDownUpLatch() {
        this(0);
    }

    public CountDownUpLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        }
        this.sync = new Sync(count);
    }

    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countUp() {
        sync.acquireShared(1);
    }


    public void countDown() {
        sync.releaseShared(1);
    }

    public long getCount() {
        return sync.getCount();
    }

    @Override
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
