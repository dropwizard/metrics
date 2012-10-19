package com.yammer.metrics;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

/**
 * A default metrics registry.
 */
public class Metrics {
    private static final MetricsRegistry DEFAULT_REGISTRY = new MetricsRegistry();
    private static final JmxReporter JMX_REPORTER = new JmxReporter(DEFAULT_REGISTRY);

    static {
        JMX_REPORTER.start();
    }

    private Metrics() { /* unused */ }

    /**
     * Returns the (static) default registry.
     *
     * @return the metrics registry
     */
    public static MetricsRegistry defaultRegistry() {
        return DEFAULT_REGISTRY;
    }
}
