package com.yammer.metrics.core;

import com.yammer.metrics.core.HealthCheck.Result;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An implementation of HealthCheckRegistry which runs the checks sequentially.
 */
public class SequentialHealthCheckRegistry implements HealthCheckRegistry {
    private final ConcurrentMap<String, HealthCheck> healthChecks = new ConcurrentHashMap<String, HealthCheck>();

    @Override public void register(HealthCheck healthCheck) {
        healthChecks.putIfAbsent(healthCheck.getName(), healthCheck);
    }

    @Override public void unregister(String name) {
        healthChecks.remove(name);
    }

    @Override public void unregister(HealthCheck healthCheck) {
        unregister(healthCheck.getName());
    }

    @Override public SortedMap<String, Result> runHealthChecks() {
        final SortedMap<String, Result> results = new TreeMap<String, Result>();
        for (Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            final Result result = entry.getValue().execute();
            results.put(entry.getKey(), result);
        }
        return Collections.unmodifiableSortedMap(results);
    }
}
