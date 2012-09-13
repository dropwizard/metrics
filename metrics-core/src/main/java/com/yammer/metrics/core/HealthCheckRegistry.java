package com.yammer.metrics.core;

import com.yammer.metrics.core.HealthCheck.Result;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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
     * Returns a set of names of all registered {@link HealthCheck} instances
     *
     * @return a set of names
     */
    public Set<String> getRegisteredNames() {
        return Collections.unmodifiableSet(healthChecks.keySet());
    }

    /**
     * Runs the registered health checks and returns a map of the results.
     *
     * @return a map of the health check results
     */
    public SortedMap<String, Result> runHealthChecks() {
        final SortedMap<String, Result> results = new TreeMap<String, Result>();
        for (Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            results.put(entry.getKey(), runHealthCheck(entry.getValue()));
        }
        return Collections.unmodifiableSortedMap(results);
    }

    /**
     * Runs HealthCheck registered under the parameter name
     *
     * @param name the name of the {@link HealthCheck} instance
     * @return health check result
     */
    public Result runHealthCheck(String name) {
        return runHealthCheck(healthChecks.get(name));
    }

    /**
     * Runs provided HealthCheck
     *
     * @param healthCheck the {@link HealthCheck} instance
     * @return health check result
     */
    private Result runHealthCheck(HealthCheck healthCheck) {
        return healthCheck.execute();
    }

}
