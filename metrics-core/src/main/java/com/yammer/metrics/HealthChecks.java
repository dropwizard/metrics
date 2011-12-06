package com.yammer.metrics;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheck.Result;
import com.yammer.metrics.core.HealthCheckRegistry;

import java.util.Map;

/**
 * A manager class for health checks.
 */
public class HealthChecks {
    private static final HealthCheckRegistry DEFAULT_REGISTRY = new HealthCheckRegistry();

    private HealthChecks() { /* unused */ }

    /**
     * Registers an application {@link HealthCheck} with a given name.
     *
     * @param healthCheck the {@link HealthCheck} instance
     */
    public static void register(HealthCheck healthCheck) {
        DEFAULT_REGISTRY.register(healthCheck);
    }

    /**
     * Runs the registered health checks and returns a map of the results.
     *
     * @return a map of the health check results
     */
    public static Map<String, Result> runHealthChecks() {
        return DEFAULT_REGISTRY.runHealthChecks();
    }

    /**
     * Returns the (static) default registry.
     *
     * @return the registry
     */
    public static HealthCheckRegistry defaultRegistry() {
        return DEFAULT_REGISTRY;
    }
}
