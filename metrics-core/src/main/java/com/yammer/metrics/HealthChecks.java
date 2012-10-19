package com.yammer.metrics;

import com.yammer.metrics.core.HealthCheckRegistry;

/**
 * A default health check registry.
 */
public class HealthChecks {
    private static final HealthCheckRegistry DEFAULT_REGISTRY = new HealthCheckRegistry();

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
