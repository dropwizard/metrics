package com.yammer.metrics;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheck.Result;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A registry for health checks.
 */
public class HealthCheckRegistry {
    private final ConcurrentMap<String, HealthCheck> healthChecks = new ConcurrentHashMap<String, HealthCheck>();

    /**
     * Registers an application {@link HealthCheck} with a given name.
     *
     * @param healthCheck the {@link HealthCheck} instance
     */
    public void register(HealthCheck healthCheck) {
        healthChecks.putIfAbsent(healthCheck.name(), healthCheck);
    }

    /**
     * Runs the registered health checks and returns a map of the results.
     *
     * @return a map of the health check results
     */
    public Map<String, Result> runHealthChecks() {
        final Map<String, Result> results = new TreeMap<String, Result>();
        for (Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            final Result result = entry.getValue().execute();
            results.put(entry.getKey(), result);
        }
        return results;
    }
}
