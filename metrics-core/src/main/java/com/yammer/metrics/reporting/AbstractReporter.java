package com.yammer.metrics.reporting;

import java.util.concurrent.ScheduledExecutorService;

import com.yammer.metrics.core.MetricsRegistry;

public abstract class AbstractReporter implements Runnable {
    protected final ScheduledExecutorService tickThread;
    protected final MetricsRegistry metricsRegistry;
    
    protected AbstractReporter(MetricsRegistry metricsRegistry, String name) {
        this.tickThread = metricsRegistry.threadPools().newScheduledThreadPool(1, name);
        this.metricsRegistry = metricsRegistry;
    }
    
    @Override
    public abstract void run();
}
