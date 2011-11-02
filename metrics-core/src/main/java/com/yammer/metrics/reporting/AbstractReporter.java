package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;

public abstract class AbstractReporter {

    protected final MetricsRegistry metricsRegistry;

    protected AbstractReporter(MetricsRegistry metricsRegistry, String name) {
        this.metricsRegistry = metricsRegistry;
    }

    /**
     * Stops the reporter and closes any internal resources.
     */
    public void shutdown() {
    }
}
