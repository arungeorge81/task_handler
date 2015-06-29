package pns.alltypes.tasks;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import pns.alltypes.thread.factory.AllAppTypesThreadFactory;

/**
 * @author arung
 */
public class DelayedTaskQueue {
    private static final Logger LOGGER = Logger.getLogger(DelayedTaskQueue.class);
    private final BlockingDeque<Runnable> RESOURCE_RUNNABLES = new LinkedBlockingDeque<Runnable>();

    private final ExecutorService service;
    private final Random RANDOM = new Random();
    private final String threadName;
    private final int delay;
    private final String executorPoolName;

    public DelayedTaskQueue(final int threadCount, final String threadName, final String executorPoolName, final int delay) {
        this.threadName = threadName;
        this.executorPoolName = executorPoolName;
        this.delay = delay;
        service = Executors.newFixedThreadPool(threadCount, new AllAppTypesThreadFactory(executorPoolName));
        init();
    }

    public void init() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    Runnable task = null;
                    try {
                        task = getRESOURCE_RUNNABLES().takeFirst();
                    } catch (final InterruptedException e1) {
                        // ignore
                    }
                    if (task != null) {
                        int delay = getRANDOM().nextInt(DelayedTaskQueue.this.getDelay());
                        delay = delay == 0 ? 1 : delay;
                        DelayedTaskQueue.LOGGER.info(String.format("%s CONSUMER  WILL BE RECREATED IN %d SECONDS ",
                                getExecutorPoolName().toUpperCase(Locale.ENGLISH), delay));
                        synchronized (this) {
                            try {
                                wait(delay);
                            } catch (final InterruptedException e) {
                                // Thread.currentThread().interrupt();
                            }
                        }

                        getService().submit(task);
                    }

                }

            }

        }, getThreadName()).start();

    }

    public void addTask(final Runnable r) {
        try {
            getRESOURCE_RUNNABLES().put(r);
        } catch (final InterruptedException e) {
            // interruptible.
        }
    }

    /**
     *
     */
    public void shutdown() {
        getService().shutdown();

    }

    public BlockingDeque<Runnable> getRESOURCE_RUNNABLES() {
        return RESOURCE_RUNNABLES;
    }

    public int getDelay() {
        return delay;
    }

    public String getExecutorPoolName() {
        return executorPoolName;
    }

    public ExecutorService getService() {
        return service;
    }

    public String getThreadName() {
        return threadName;
    }

    public Random getRANDOM() {
        return RANDOM;
    }

}
