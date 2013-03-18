package com.yammer.metrics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractPollingReporter implements Reporter {
    /**
     * A simple named thread factory.
     */
    @SuppressWarnings("NullableProblems")
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

    private final MetricRegistry registry;
    private final ScheduledExecutorService executor;
    private final MetricFilter filter;

    /**
     * Creates a new {@link AbstractPollingReporter} instance.
     *
     * @param registry the {@link com.yammer.metrics.MetricRegistry} containing the metrics this reporter will report
     * @param name     the reporter's name
     * @param filter   the filter for which metrics to report
     */
    protected AbstractPollingReporter(MetricRegistry registry, String name, MetricFilter filter) {
        this.registry = registry;
        this.filter = filter;
        this.executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name));
    }

    /**
     * Starts the reporter polling at the given period.
     *
     * @param period the amount of time between polls
     * @param unit   the unit for {@code period}
     */
    public void start(long period, TimeUnit unit) {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                report(registry.getGauges(filter),
                       registry.getCounters(filter),
                       registry.getHistograms(filter),
                       registry.getMeters(filter),
                       registry.getTimers(filter));
            }
        }, period, period, unit);
    }

    public void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // do nothing
        }
    }
}
