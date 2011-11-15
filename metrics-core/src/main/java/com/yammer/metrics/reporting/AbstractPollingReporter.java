package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPollingReporter extends AbstractReporter implements Runnable {
    private final ScheduledExecutorService executor;

    protected AbstractPollingReporter(MetricsRegistry registry, String name) {
        super(registry);
        this.executor = registry.threadPools().newScheduledThreadPool(1, name);
    }

    public void start(long pollingTime, TimeUnit pollingTimeUnit) {
        executor.scheduleWithFixedDelay(this, pollingTime, pollingTime, pollingTimeUnit);
    }

    public void shutdown(long waitTime, TimeUnit waitTimeMillis) throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(waitTime, waitTimeMillis);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
