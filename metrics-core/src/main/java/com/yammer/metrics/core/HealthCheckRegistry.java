package com.yammer.metrics.core;

import java.util.SortedMap;

/**
 * A registry for health checks.
 */
public interface HealthCheckRegistry {
    /**
     * Registers an application {@link HealthCheck}.
     *
     * @param healthCheck the {@link HealthCheck} instance
     */
    void register(HealthCheck healthCheck);

    /**
     * Unregisters the application {@link HealthCheck} with the given name.
     *
     * @param name the name of the {@link HealthCheck} instance
     */
    void unregister(String name);

    /**
     * Unregisters the given {@link HealthCheck}.
     *
     * @param healthCheck    a {@link HealthCheck}
     */
    void unregister(HealthCheck healthCheck);

    /**
     * Runs the registered health checks and returns a map of the results.
     *
     * @return a map of the health check results
     */
    SortedMap<String, HealthCheck.Result> runHealthChecks();
}
