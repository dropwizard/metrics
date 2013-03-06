package com.yammer.metrics;

import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.SequentialHealthCheckRegistry;

/**
 * A default health check registry.
 */
public class HealthChecks {
    private static final HealthCheckRegistry DEFAULT_REGISTRY = new SequentialHealthCheckRegistry();

    private HealthChecks() { /* unused */ }

    /**
     * Returns the (static) default registry.
     *
     * @return the registry
     */
    public static HealthCheckRegistry defaultRegistry() {
        return DEFAULT_REGISTRY;
    }
}
