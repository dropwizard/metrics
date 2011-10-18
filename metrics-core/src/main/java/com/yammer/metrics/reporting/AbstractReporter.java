package com.yammer.metrics.reporting;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    
    /**
     * Starts reporter output.
     *
     * @param period the period between successive displays
     * @param unit   the time unit of {@code period}
     */
    public abstract void start(long period, TimeUnit unit);
    
    /**
     * Stops and cleans up reporter output
     */
    public void shutdown(){
    	
    }
}
