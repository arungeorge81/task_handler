package pns.alltypes.thread.factory;

import java.util.concurrent.ThreadFactory;

/**
 * A thread factory for all
 * @author arung
 */
public class AllAppTypesThreadFactory implements ThreadFactory {
    private final String poolName;

    public AllAppTypesThreadFactory(final String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        return new AllTypesAppThread(runnable, poolName);
    }
}