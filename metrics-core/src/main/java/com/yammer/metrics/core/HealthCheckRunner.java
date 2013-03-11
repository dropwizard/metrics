package com.yammer.metrics.core;

import java.util.SortedMap;

public interface HealthCheckRunner {

    /**
     * Runs health checks and returns a map of the results.
     *
     * @return a map of the health check results
     */
    public SortedMap<String, HealthCheck.Result> run();

}
