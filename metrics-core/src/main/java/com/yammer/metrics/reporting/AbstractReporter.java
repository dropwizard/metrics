package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.MetricsRegistryListener;

/**
 * The base class for all metric reporters.
 */
public abstract class AbstractReporter {
    private final MetricsRegistry metricsRegistry;

    /**
     * Creates a new {@link AbstractReporter} instance.
     *
     * @param registry the {@link MetricsRegistry} containing the metrics this reporter will
     *                 report
     */
    protected AbstractReporter(MetricsRegistry registry) {
        this.metricsRegistry = registry;
    }

    /**
     * Start the reporter by adding it as a listener on the registry
     */
    /*
    public void start() {
        metricsRegistry.addListener(this);
    }*/

    /**
     * Stops the reporter and closes any internal resources.
     */
    public void shutdown() {
        //metricsRegistry.removeListener(this);
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
