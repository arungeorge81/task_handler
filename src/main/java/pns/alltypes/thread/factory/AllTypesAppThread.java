/*
 * PnsAppThread.java
 * @author arung
 **********************************************************************

             Copyright (c) 2004 - 2014 by Sling Media, Inc.

All rights are reserved.  Reproduction in whole or in part is prohibited
without the written consent of the copyright owner.

Sling Media, Inc. reserves the right to make changes without notice at any time.

Sling Media, Inc. makes no warranty, expressed, implied or statutory, including
but not limited to any implied warranty of merchantability of fitness for any
particular purpose, or that the use will not infringe any third party patent,
copyright or trademark.

Sling Media, Inc. must not be liable for any loss or damage arising from its
use.

This Copyright notice may not be removed or modified without prior
written consent of Sling Media, Inc.

 ***********************************************************************/
package pns.alltypes.thread.factory;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * @author arung
 */
public class AllTypesAppThread extends Thread {

    public static final String DEFAULT_NAME = "PnsAppThread";
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
