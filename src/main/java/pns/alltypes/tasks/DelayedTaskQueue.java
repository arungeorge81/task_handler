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
 * A task queue with a single thread which allows to do the task with simple wait with timeout. This avoids the heavy
 * threadpool executor and uses a single thread for this.
 * @author arung
 */
public class DelayedTaskQueue {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(DelayedTaskQueue.class);

    /** The resource runnables which is used to queue the tasks */
    private final BlockingDeque<Runnable> RESOURCE_RUNNABLES = new LinkedBlockingDeque<Runnable>();

    /** The service. */
    private final ExecutorService service;

    /** The random. */
    private final Random RANDOM = new Random();

    /** The thread name. */
    private final String threadName;

    /** The delay. */
    private final int delay;

    /** The executor pool name. */
    private final String executorPoolName;

    /**
     * Instantiates a new delayed task queue.
     * @param threadCount
     *            the thread count
     * @param threadName
     *            the thread name
     * @param executorPoolName
     *            the executor pool name
     * @param delay
     *            the delay
     */
    public DelayedTaskQueue(final int threadCount, final String threadName, final String executorPoolName, final int delay) {
        this.threadName = threadName;
        this.executorPoolName = executorPoolName;
        this.delay = delay;
        service = Executors.newFixedThreadPool(threadCount, new AllAppTypesThreadFactory(executorPoolName));
        init();
    }

    /**
     * Inits the
     */
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

    /**
     * Adds the task for delayed execution
     * @param r
     *            the r
     */
    public void addTask(final Runnable r) {
        try {
            getRESOURCE_RUNNABLES().put(r);
        } catch (final InterruptedException e) {
            // interruptible.
        }
    }

    /**
     * Shutdown the thread
     */
    public void shutdown() {
        getService().shutdown();

    }

    /**
     * Gets the resource runnables which needs to be submitted
     * @return the resource runnables
     */
    private BlockingDeque<Runnable> getRESOURCE_RUNNABLES() {
        return RESOURCE_RUNNABLES;
    }

    /**
     * Gets the delay.
     * @return the delay
     */
    private int getDelay() {
        return delay;
    }

    /**
     * Gets the executor pool name.
     * @return the executor pool name
     */
    private String getExecutorPoolName() {
        return executorPoolName;
    }

    /**
     * Gets the service.
     * @return the service
     */
    private ExecutorService getService() {
        return service;
    }

    /**
     * Gets the thread name.
     * @return the thread name
     */
    private String getThreadName() {
        return threadName;
    }

    /**
     * Gets the random number
     * @return the random
     */
    private Random getRANDOM() {
        return RANDOM;
    }

}
