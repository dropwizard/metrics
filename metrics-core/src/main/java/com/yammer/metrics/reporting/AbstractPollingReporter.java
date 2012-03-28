package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.ThreadPools;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An abstract base class for all reporter implementations which periodically poll registered
 * metrics (e.g., to send the data to another service).
 */
public abstract class AbstractPollingReporter extends AbstractReporter implements Runnable {
    static final long DEFAULT_PERIOD = 1;
    static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.MINUTES;

    protected ScheduledExecutorService executor;
    protected long period;
    protected TimeUnit unit;

    /**
     * Creates a new {@link AbstractPollingReporter} instance.
     *
     * @param registries    the {@link MetricsRegistry} containing the metrics this reporter will
     *                    report
     * @see AbstractReporter#AbstractReporter(MetricsRegistry)
     */
    protected AbstractPollingReporter(Set<MetricsRegistry> registries, String name, long period, TimeUnit unit) {
        super(registries, name);
        this.period = period;
        this.unit = unit;
        this.executor = ThreadPools.getInstance().newScheduledThreadPool(1, name);
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
    }

    /**
     * The method called when a a poll is scheduled to occur.
     */
    @Override
    public abstract void run();
}
