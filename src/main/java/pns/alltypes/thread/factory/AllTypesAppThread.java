package pns.alltypes.thread.factory;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * Creation of thread logged as in the book "Best practices in Concurrency"
 * @author arung
 */
public class AllTypesAppThread extends Thread {

    public static final String DEFAULT_NAME = "AllTypesAppThread";
    private static final AtomicInteger created = new AtomicInteger();
    private static final AtomicInteger alive = new AtomicInteger();
    private static final Logger LOGGER = Logger.getLogger(AllTypesAppThread.class);

    public AllTypesAppThread(final Runnable r) {
        this(r, AllTypesAppThread.DEFAULT_NAME);
    }

    public AllTypesAppThread(final Runnable runnable, final String name) {
        super(runnable, name + "-" + AllTypesAppThread.created.incrementAndGet());
        setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                AllTypesAppThread.LOGGER.error("UNCAUGHT in thread " + t.getName(), e);
            }
        });
    }

    @Override
    public void run() {
        // Copy debug flag to ensure consistent value throughout.

        if (AllTypesAppThread.LOGGER.isTraceEnabled()) {
            AllTypesAppThread.LOGGER.debug("Created " + getName());
        }
        try {
            AllTypesAppThread.alive.incrementAndGet();
            super.run();
        } finally {
            AllTypesAppThread.alive.decrementAndGet();
            if (AllTypesAppThread.LOGGER.isTraceEnabled()) {
                AllTypesAppThread.LOGGER.trace("Exiting " + getName());
            }
        }
    }

    public static int getThreadsCreated() {
        return AllTypesAppThread.created.get();
    }

    public static int getThreadsAlive() {
        return AllTypesAppThread.alive.get();
    }

}
