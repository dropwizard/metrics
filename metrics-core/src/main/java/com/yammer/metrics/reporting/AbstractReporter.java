package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;

/**
 * The base class for all metric reporters.
 */
public abstract class AbstractReporter {
    private final MetricsRegistry metricsRegistry;

    /**
     * Creates a new {@link AbstractReporter} instance.
     *
     * @param registry    the {@link MetricsRegistry} containing the metrics this reporter will
     *                    report
     */
    protected AbstractReporter(MetricsRegistry registry) {
        this.metricsRegistry = registry;
    }

    /**
     * Stops the reporter and closes any internal resources.
     */
    public void shutdown() {
        // nothing to do here
    }

    /**
     * Returns the reporter's {@link MetricsRegistry}.
     *
     * @return the reporter's {@link MetricsRegistry}
     */
    protected MetricsRegistry getMetricsRegistry() {
        return metricsRegistry;
    }
}
