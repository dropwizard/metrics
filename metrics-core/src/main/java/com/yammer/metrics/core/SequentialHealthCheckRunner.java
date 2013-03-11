package com.yammer.metrics.core;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

public class SequentialHealthCheckRunner implements HealthCheckRunner {

    private ConcurrentMap<String, HealthCheck> healthChecks;

    public SequentialHealthCheckRunner(ConcurrentMap<String, HealthCheck> healthChecks) {
        this.healthChecks = healthChecks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<String, HealthCheck.Result> run() {

        final SortedMap<String, HealthCheck.Result> results = new TreeMap<String, HealthCheck.Result>();
        for (Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            final HealthCheck.Result result = entry.getValue().execute();
            results.put(entry.getKey(), result);
        }
        return Collections.unmodifiableSortedMap(results);

    }

}
