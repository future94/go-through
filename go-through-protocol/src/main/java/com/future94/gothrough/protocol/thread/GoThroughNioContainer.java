package com.future94.gothrough.protocol.thread;

import com.future94.gothrough.common.utils.CountDownUpLatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author weilai
 */
@Slf4j
public class GoThroughNioContainer {

    private static final GoThroughNioContainer INSTANCE = new GoThroughNioContainer();

    private GoThroughNioContainer() {
    }

    public static GoThroughNioContainer getInstance() {
        return INSTANCE;
    }

    private volatile Thread myThread = null;

    private volatile boolean alive = false;

    private volatile boolean cancel = false;

    private volatile Selector selector;

    private long selectTimeout = 10L;

    private long wakeupSleepNanos = 1000000L;

    private final Object selectorLock = new Object();

    private final Map<SelectableChannel, NioProcessNode> channelProcessNodeMap = new ConcurrentHashMap<>();

    private final CountDownUpLatch countDownUpLatch = new CountDownUpLatch();

    private final ExecutorService executorService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), GoThroughThreadFactory.create("nio-container-process"), new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * @param ops 依据以下值进行或运算进行最后结果设定，并且 {@code channel} 要支持相应的动作
     *            {@link SelectionKey#OP_ACCEPT}
     *            {@link SelectionKey#OP_CONNECT}
     *            {@link SelectionKey#OP_READ}
     *            {@link SelectionKey#OP_WRITE}
     */
    public static void register(SelectableChannel socketChannel, int ops, GoThroughNIORunnable runnable) throws IOException {
        getInstance().register0(socketChannel, ops, runnable);
    }

    /**
     * 根据 {@link SelectionKey} 恢复监听事件的注册
     *
     * @param key 原始的key
     * @param ops 要与通过 {@link #register(SelectableChannel, int, GoThroughNIORunnable)}
     *            注册的事件统一
     * @throws IOException
     */
    public static boolean reRegisterByKey(SelectionKey key, int ops) {
        return INSTANCE.reRegisterByKey0(key, ops);
    }

    /**
     * 根据 {@link SelectionKey} 恢复监听事件的注册
     *
     * @param key 原始的key
     * @param ops 要与通过 {@link #register0(SelectableChannel, int, GoThroughNIORunnable)}
     *            注册的事件统一
     * @throws IOException
     */
    public boolean reRegisterByKey0(SelectionKey key, int ops) {
        Objects.requireNonNull(key, "key non null");

        if (key.selector() != this.selector) {
            log.warn("this SelectionKey [{}] is not belong GoThroughNioContainer's selector", key.toString());
            return false;
        }

        if (!key.isValid()) {
            return false;
        }

        // 通过事件和源码分析，恢复注册是通过updateKeys.addLast进行，虽然没有被阻塞，但是需要进行一次唤醒才可以成功恢复事件监听
        // 因无法获知是否成功注入selector，所以必须要进行一次唤醒操作，并且没有阻塞的问题，所以这里不通过countWaitLatch进行同步
        key.interestOps(ops);

        try {
            this.getWakeupSelector();
        } catch (IOException e) {
            // 出错了交给其他的流程逻辑，这里只进行一次唤醒
        }

        return true;
    }

    private void register0(SelectableChannel socketChannel, int ops, GoThroughNIORunnable runnable) throws IOException {
        Objects.requireNonNull(socketChannel, "socketChannel non null");
        try {
            channelProcessNodeMap.put(socketChannel, NioProcessNode.of(socketChannel, ops, runnable));
            socketChannel.configureBlocking(false);
            countDownUpLatch.countUp();
            // 这里有个坑点，如果在select中，这里会被阻塞
            socketChannel.register(getWakeupSelector(), ops);
        } catch (Throwable e) {
            this.channelProcessNodeMap.remove(socketChannel);
            throw e;
        } finally {
            this.countDownUpLatch.countDown();
        }
    }

    public static void release(SelectableChannel channel) {
        getInstance().release0(channel);
    }

    public void release0(SelectableChannel channel) {
        if (Objects.isNull(channel)) {
            return;
        }
        channelProcessNodeMap.remove(channel);
        SelectionKey key = channel.keyFor(this.selector);
        if (Objects.nonNull(key)) {
            key.cancel();
        }
    }

    public Selector getWakeupSelector() throws IOException {
        return this.getSelector().wakeup();
    }

    public Selector getSelector() throws IOException {
        // 判空、返回逻辑，按第一次取值进行，缺点是不能判断是否已经关闭，但与 this.cancel()
        // 方法中的执行顺序来看，会先被设置为null，再去close，所以可以大概率认为若不为null即为没有关闭
        Selector selector = this.selector;
        if (Objects.isNull(selector)) {
            synchronized (this.selectorLock) {
                // 二次校验
                // 若是主动退出，则不在创建，避免退出时有新任务而被重启，若要重新启用，则需要主动调用 start() 方法来启动
                if (Objects.isNull(this.selector) && !this.cancel) {
                    this.selector = Selector.open();
                    this.start();
                }
            }
            selector = this.selector;
            if (Objects.isNull(selector)) {
                throw new IOException("NioHallows's selector is closed");
            }
        }
        return selector;
    }

    public synchronized void start() {

        this.cancel = false;
        this.alive = true;

        Thread myThread = this.myThread;
        if (myThread == null || !myThread.isAlive()) {
            this.myThread = new ProcessThread();
            this.myThread.start();
            log.info("NioContainer is started!");
        }
    }

    public void cancel() {
        // 假设A线程执行到了 this.selector = Selector.open() 但是调用 this.cancel()
        // 方法的B线程抢占cpu成功，并一直到执行完成，此时A线程抢占CPU继续执行，又会进行重启，与关停项目时的关停期望不同。
        //
        // 此处锁定 this.selectorLock 后再去设置 this.canceled，形成与 this.getSelector()
        // 的线程同步，同时避免了被动调用 this.start() 时与 this.cancel() 的同步问题，最终可关闭。
        // 虽与主动调用 this.start() 有不同步的风险，但 this.start() 、 this.cancel()
        // 主动调用的场景有极大对立性，所以不进行过多的关照。
        //
        // 注意：若 this.cancel() 添加了synchronized，存在死锁的可能！！！
        synchronized (this.selectorLock) {
            this.cancel = true;
        }

        log.info("GoThroughNioContainer cancel");

        this.alive = false;

        Selector selector;
        if ((selector = this.selector) != null) {
            this.selector = null;
            try {
                selector.close();
            } catch (IOException e) {
                // do nothing
            }
        }

        Thread myThread;
        if ((myThread = this.myThread) != null) {
            this.myThread = null;
            myThread.interrupt();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    static class NioProcessNode {

        private SelectableChannel channel;

        private int interestOps;

        private GoThroughNIORunnable runnable;

        public void process(SelectionKey key) {
            runnable.process(key, interestOps, channel);
        }
    }

    class ProcessThread extends Thread {

        @Override
        public void run() {
            for (; alive; ) {
                // 给注册事务一个时间，如果等待时间太长（可能需要注入的太多），就跳出再去获取新事件，防止饿死
                try {
                    countDownUpLatch.await(wakeupSleepNanos, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    log.warn("selector wait register timeout");
                }

                Selector selector;
                try {
                    selector = getSelector();

                    // 采用有期限的监听，以免线程太快，没有来的及注册，就永远阻塞在那里了
                    int select = selector.select(selectTimeout);
                    if (select <= 0) {
                        continue;
                    }
                } catch (IOException e) {
                    log.error("NioHallows run exception", e);
                    continue;
                }

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                for (; iterator.hasNext(); ) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    try {
                        key.interestOps(0);
                    } catch (Exception e) {
                        // do no thing
                    }

                    NioProcessNode nioProcessNode = channelProcessNodeMap.get(key.channel());
                    if (Objects.isNull(nioProcessNode)) {
                        key.cancel();
                        continue;
                    }

                    executorService.execute(() -> nioProcessNode.process(key));

                }
            }
        }
    }
}
