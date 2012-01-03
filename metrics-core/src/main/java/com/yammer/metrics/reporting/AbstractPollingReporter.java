package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An abstract base class for all reporter implementations which periodically poll registered
 * metrics (e.g., to send the data to another service).
 */
public abstract class AbstractPollingReporter extends AbstractReporter implements Runnable {
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
        this.executor = registry.newScheduledThreadPool(1, name);
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

    /**
     * Shuts down the reporter polling, waiting the specific amount of time for any current polls to
     * complete.
     *
     * @param timeout    the maximum time to wait
     * @param unit       the unit for {@code timeout}
     * @throws InterruptedException if interrupted while waiting
     */
    public void shutdown(long timeout, TimeUnit unit) throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(timeout, unit);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        super.shutdown();
    }

    /**
     * The method called when a a poll is scheduled to occur.
     */
    @Override
    public abstract void run();
}
