package pns.alltypes.tasks;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import pns.alltypes.thread.factory.AllAppTypesThreadFactory;

/**
 * This class aims at blocking the caller who submits the job based on the fact there is no avialable threads to execute
 * This helps in implementing a backoff algorithm.
 * @author arung
 */
public class ZeroRejectionThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = Logger.getLogger(ZeroRejectionThreadPoolExecutor.class);
    private final Lock aLock = new ReentrantLock();
    private final Condition condVar = aLock.newCondition();

    private final AtomicInteger countTasks = new AtomicInteger(0);
    private volatile int maxTasksAllowed;

    /**
     * @param nThreads
     * @param nThreads2
     * @param keeppAliveTime
     * @param minutes
     * @param synchronousQueue
     * @param pnsAppThreadFactory
     */
    public ZeroRejectionThreadPoolExecutor(final int nThreads, final int nThreads2, final long keeppAliveTime, final TimeUnit keepAlive,
            final SynchronousQueue<Runnable> synchronousQueue, final AllAppTypesThreadFactory pnsAppThreadFactory) {
        super(nThreads, nThreads2, keeppAliveTime, keepAlive, synchronousQueue, pnsAppThreadFactory);
        if (ZeroRejectionThreadPoolExecutor.LOGGER.isTraceEnabled()) {
            ZeroRejectionThreadPoolExecutor.LOGGER.trace(String.format("Setting maxTasks to %d", nThreads2));
        }
        this.maxTasksAllowed = nThreads2 - 2;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable
     * , java.lang.Throwable)
     */
    @Override
    public void afterExecute(final Runnable r, final Throwable t) {

        try {
            aLock.lock();
            final int decrementedValue = countTasks.decrementAndGet();
            if (ZeroRejectionThreadPoolExecutor.LOGGER.isTraceEnabled()) {
                ZeroRejectionThreadPoolExecutor.LOGGER.trace(String.format("Decremented to %d", decrementedValue));
            }
            condVar.signalAll();
            // notifyAll();
        } finally {
            aLock.unlock();

        }
    }

    // maxtask rached still in while loop execute afterexecute runnable
    // threadpoolexecutor extend thread concurrency locks synchronization
    // threadfactory monitor still kept not giving up monitor
    // wait counter random interval non block structured locking reentrant
    // lock
    /*
     * (non-Javadoc)
     *
     * @see
     * java.util.concurrent.ThreadPoolExecutor#execute(java.lang.Runnable)
     */
    @Override
    public void execute(final Runnable command) {
        int currentTaskCount = 0;

        try {
            aLock.lock();

            try {
                while ((currentTaskCount = countTasks.get()) == maxTasksAllowed) {
                    if (ZeroRejectionThreadPoolExecutor.LOGGER.isTraceEnabled()) {
                        ZeroRejectionThreadPoolExecutor.LOGGER.trace(String.format("Reached max task allowed for pool %s", getThreadFactory().toString()));
                    }
                    condVar.await();
                }
                condVar.signalAll();
                super.execute(command);

            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        } finally {

            if (ZeroRejectionThreadPoolExecutor.LOGGER.isTraceEnabled()) {
                ZeroRejectionThreadPoolExecutor.LOGGER.trace(String.format("Going to increment tasks to %d", currentTaskCount + 1));
            }
            countTasks.incrementAndGet();
            aLock.unlock();

        }

    }

}