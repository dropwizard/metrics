package com.yammer.metrics.core;

import java.util.*;
import java.util.concurrent.*;

/**
 * An implementation of {@link HealthCheckRegistry} which runs {@link HealthCheck} in parallel
 * using an {@link java.util.concurrent.ExecutorService}
 */
public class ParallelHealthCheckRegistry implements HealthCheckRegistry{
    private final ConcurrentMap<String, HealthCheck> healthChecks = new ConcurrentHashMap<String, HealthCheck>();
    private final ExecutorService executor;
    private final long timeout;
    private final TimeUnit timeUnit;

    public ParallelHealthCheckRegistry(ExecutorService executor, long timeout, TimeUnit timeUnit){
        this.executor = executor;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override public void register(HealthCheck healthCheck) {
        healthChecks.putIfAbsent(healthCheck.getName(), healthCheck);
    }

    @Override public void unregister(String name) {
        healthChecks.remove(name);
    }

    @Override public void unregister(HealthCheck healthCheck) {
        unregister(healthCheck.getName());
    }

    @Override public SortedMap<String, HealthCheck.Result> runHealthChecks() {
        Map<String, Future<HealthCheck.Result>> futureResults = new HashMap<String, Future<HealthCheck.Result>>();
        for(final HealthCheck check : healthChecks.values()){
            futureResults.put(check.getName(), executor.submit(new Callable<HealthCheck.Result>() {
                @Override public HealthCheck.Result call() throws Exception {
                    return check.execute();
                }
            }));
        }

        SortedMap<String, HealthCheck.Result> results = new TreeMap<String, HealthCheck.Result>();
        for(Map.Entry<String, Future<HealthCheck.Result>> result : futureResults.entrySet()){
            try {
                results.put(result.getKey(), result.getValue().get(timeout, timeUnit));
            } catch (Exception e){
                results.put(result.getKey(), HealthCheck.Result.unhealthy(e));
            }
        }

        return results;
    }
}
