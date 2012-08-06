package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An abstract base class for all reporter implementations which periodically poll registered
 * metrics (e.g., to send the data to another service).
 */
public abstract class AbstractPollingReporter extends AbstractReporter implements Runnable {
    /**
     * A simple named thread factory.
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private NamedThreadFactory(String name) {
            final SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = "metrics-" + name + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    private final ScheduledExecutorService executor;

    /**
     * Creates a new {@link AbstractPollingReporter} instance.
     *
     * @param registry    the {@link MetricsRegistry} containing the metrics this reporter will
     *                    report
     * @param name        the reporter's name
     * @see AbstractReporter#AbstractReporter(MetricsRegistry)
     */
    protected AbstractPollingReporter(MetricsRegistry registry, String name) {
        super(registry);
        this.executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name));
    }

    /**
     * Starts the reporter polling at the given period.
     *
     * @param period    the amount of time between polls
     * @param unit      the unit for {@code period}
     */
    public void start(long period, TimeUnit unit) {
        executor.scheduleWithFixedDelay(this, period, period, unit);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // do nothing
        }
    }

    /**
     * The method called when a a poll is scheduled to occur.
     */
    @Override
    public abstract void run();
}
