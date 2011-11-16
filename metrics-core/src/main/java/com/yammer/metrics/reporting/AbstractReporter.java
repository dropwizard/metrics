package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;

public abstract class AbstractReporter {

    protected final MetricsRegistry metricsRegistry;

    protected AbstractReporter(MetricsRegistry registry) {
        this.metricsRegistry = registry;
    }

    /**
     * Stops the reporter and closes any internal resources.
     */
    public void shutdown() {
    }
}
