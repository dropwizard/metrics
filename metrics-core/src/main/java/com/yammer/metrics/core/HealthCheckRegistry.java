package com.yammer.metrics.core;

import com.yammer.metrics.core.HealthCheck.Result;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A registry for health checks.
 */
public class HealthCheckRegistry {
    private final ConcurrentMap<String, HealthCheck> healthChecks = new ConcurrentHashMap<String, HealthCheck>();

    /**
     * Registers an application {@link HealthCheck}.
     *
     * @param healthCheck the {@link HealthCheck} instance
     */
    public void register(HealthCheck healthCheck) {
        healthChecks.putIfAbsent(healthCheck.getName(), healthCheck);
    }

    /**
     * Unregisters the application {@link HealthCheck} with the given name.
     *
     * @param name the name of the {@link HealthCheck} instance
     */
    public void unregister(String name) {
        healthChecks.remove(name);
    }

    /**
     * Unregisters the given {@link HealthCheck}.
     *
     * @param healthCheck    a {@link HealthCheck}
     */
    public void unregister(HealthCheck healthCheck) {
        unregister(healthCheck.getName());
    }

    /**
     * Returns registered health check count
     *
     * @return size of internal registry
     */
    public int size() {
        return healthChecks.size();
    }

    /**
     * Returns {@link HealthCheckRunner} Runner
     *
     * This implementation creates the default {@link SequentialHealthCheckRunner}
     *
     * @return health check runner
     */
    protected HealthCheckRunner getHealthCheckRunner() {
        return new SequentialHealthCheckRunner(healthChecks);
    }

    /**
     * Runs the registered health checks and returns a map of the results.
     *
     * @return a map of the health check results
     */
    public SortedMap<String, Result> runHealthChecks() {
        return getHealthCheckRunner().run();
    }

}
