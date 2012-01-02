package com.yammer.metrics.util;

import com.yammer.metrics.core.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When a thread throws an Exception that was not caught, a DeathRattleExceptionHandler will
 * increment a counter signalling a thread has died and print out the name and stack trace of the
 * thread.
 * <p/>
 * This makes it easy to build alerts on unexpected Thread deaths and fine grained used quickens
 * debugging in production.
 * <p/>
 * You can also set a DeathRattleExceptionHandler as the default exception handler on all threads,
 * allowing you to get information on Threads you do not have direct control over.
 * <p/>
 * Usage is straightforward:
 * <p/>
 * <pre><code>
 * final Counter c = Metrics.newCounter(MyRunnable.class, "thread-deaths");
 * Thread.UncaughtExceptionHandler exHandler = new DeathRattleExceptionHandler(c);
 * final Thread myThread = new Thread(myRunnable, "MyRunnable");
 * myThread.setUncaughtExceptionHandler(exHandler);
 * </code></pre>
 * <p/>
 * Setting the global default exception handler should be done first, like so:
 * <p/>
 * <pre><code>
 * final Counter c = Metrics.newCounter(MyMainClass.class, "unhandled-thread-deaths");
 * Thread.UncaughtExceptionHandler ohNoIDidntKnowAboutThis = new DeathRattleExceptionHandler(c);
 * Thread.setDefaultUncaughtExceptionHandler(ohNoIDidntKnowAboutThis);
 * </code></pre>
 */
public class DeathRattleExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeathRattleExceptionHandler.class);

    private final Counter deathRattle;

    public DeathRattleExceptionHandler(Counter deathRattle) {
        this.deathRattle = deathRattle;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        deathRattle.inc();
        LOGGER.error("Uncaught exception on thread " + t, e);
    }
}
