package com.codahale.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Locale;
import java.util.SortedMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The abstract base class for all scheduled reporters (i.e., reporters which process a registry's
 * metrics periodically).
 *
 * @see ConsoleReporter
 * @see CsvReporter
 * @see Slf4jReporter
 */
public abstract class ScheduledReporter implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledReporter.class);
    private static final AtomicInteger FACTORY_ID = new AtomicInteger();

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
    private final double durationFactor;
    private final String durationUnit;
    private final double rateFactor;
    private final String rateUnit;

    private final String taskName;
    private final Object futureLock = new Object();
    private Future scheduledReportingJob;

    protected ScheduledReporter(MetricRegistry registry,
                                String name,
                                MetricFilter filter,
                                TimeUnit rateUnit,
                                TimeUnit durationUnit) {
        this.registry = registry;
        this.filter = filter;
        this.executor = createExecutorService(name);
        this.rateFactor = rateUnit.toSeconds(1);
        this.rateUnit = calculateRateUnit(rateUnit);
        this.durationFactor = 1.0 / durationUnit.toNanos(1);
        this.durationUnit = durationUnit.toString().toLowerCase(Locale.US);
        this.taskName = createTaskName();
    }

    protected ScheduledReporter(MetricRegistry registry,
                                ScheduledExecutorService executor,
                                MetricFilter filter,
                                TimeUnit rateUnit,
                                TimeUnit durationUnit) {
        this.registry = registry;
        this.filter = filter;

        if (executor != null) {
            this.executor = executor;

        } else {
            this.executor = createExecutorService(getClass().getSimpleName());
        }

        this.rateFactor = rateUnit.toSeconds(1);
        this.rateUnit = calculateRateUnit(rateUnit);
        this.durationFactor = 1.0 / durationUnit.toNanos(1);
        this.durationUnit = durationUnit.toString().toLowerCase(Locale.US);
        this.taskName = createTaskName();
    }

    private ScheduledExecutorService createExecutorService(String name) {
        return Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name + '-' + FACTORY_ID.incrementAndGet()));
    }

    private String createTaskName() {
        return String.format("%s.report", this.getClass().getCanonicalName());
    }

    /**
     * Starts the reporter polling at the given period.
     *
     * @param period the amount of time between polls
     * @param unit   the unit for {@code period}
     */
    public void start(long period, TimeUnit unit) {
        synchronized (futureLock) {
            if (scheduledReportingJob != null) {
                stop();
            }

            if (executor.isShutdown()) {
                throw new IllegalStateException("Metric Reporter has been shut down.");
            }

            scheduledReportingJob = executor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    report();
                }
                public String toString() {
                    return taskName;
                }
            }, period, period, unit);
        }
    }

    /**
     * Stops the reporter.
     */
    public void stop() {
        synchronized (futureLock) {
            if (scheduledReportingJob != null) {
                scheduledReportingJob.cancel(false);
                scheduledReportingJob = null;
            }
        }
    }

    /**
     * Stops the reporter and shuts down its thread of execution.
     */
    public void close() {
        synchronized (futureLock) {
            stop();
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                // do nothing
            }
        }
    }

    /**
     * Report the current values of all metrics in the registry.
     */
    public synchronized void report() {
        try {
            report(registry.getGauges(filter),
                    registry.getCounters(filter),
                    registry.getHistograms(filter),
                    registry.getMeters(filter),
                    registry.getTimers(filter));
        } catch (Exception e) {
            LOGGER.error("Error reporting metrics", e);
        }
    }

    /**
     * Called periodically by the polling thread. Subclasses should report all the given metrics.
     *
     * @param gauges     all of the gauges in the registry
     * @param counters   all of the counters in the registry
     * @param histograms all of the histograms in the registry
     * @param meters     all of the meters in the registry
     * @param timers     all of the timers in the registry
     */
    public abstract void report(SortedMap<String, Gauge> gauges,
                                SortedMap<String, Counter> counters,
                                SortedMap<String, Histogram> histograms,
                                SortedMap<String, Meter> meters,
                                SortedMap<String, Timer> timers);

    protected String getRateUnit() {
        return rateUnit;
    }

    protected String getDurationUnit() {
        return durationUnit;
    }

    protected double convertDuration(double duration) {
        return duration * durationFactor;
    }

    protected double convertRate(double rate) {
        return rate * rateFactor;
    }

    private String calculateRateUnit(TimeUnit unit) {
        final String s = unit.toString().toLowerCase(Locale.US);
        return s.substring(0, s.length() - 1);
    }

}
