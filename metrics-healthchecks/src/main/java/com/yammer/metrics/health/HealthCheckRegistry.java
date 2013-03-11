package com.yammer.metrics.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static com.yammer.metrics.health.HealthCheck.Result;

/**
 * A registry for health checks.
 */
public class HealthCheckRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckRegistry.class);

    private final ConcurrentMap<String, HealthCheck> healthChecks;

    /**
     * Creates a new {@link HealthCheckRegistry}.
     */
    public HealthCheckRegistry() {
        this.healthChecks = new ConcurrentHashMap<String, HealthCheck>();
    }

    /**
     * Registers an application {@link HealthCheck}.
     *
     * @param name        the name of the health check
     * @param healthCheck the {@link HealthCheck} instance
     */
    public void register(String name, HealthCheck healthCheck) {
        healthChecks.putIfAbsent(name, healthCheck);
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
     * Returns a set of the names of all registered health checks.
     *
     * @return the names of all registered health checks
     */
    public SortedSet<String> getNames() {
        return Collections.unmodifiableSortedSet(new TreeSet<String>(healthChecks.keySet()));
    }

    public HealthCheck.Result runHealthCheck(String name) {
        final HealthCheck healthCheck = healthChecks.get(name);
        if (healthCheck == null) {
            throw new NoSuchElementException("No health check named " + name + " exists");
        }
        return healthCheck.execute();
    }

    /**
     * Runs the registered health checks and returns a map of the results.
     *
     * @return a map of the health check results
     */
    public SortedMap<String, HealthCheck.Result> runHealthChecks() {
        final SortedMap<String, HealthCheck.Result> results = new TreeMap<String, HealthCheck.Result>();
        for (Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            final Result result = entry.getValue().execute();
            results.put(entry.getKey(), result);
        }
        return Collections.unmodifiableSortedMap(results);
    }

    /**
     * Runs the registered health checks in parallel and returns a map of the results.
     *
     * @return a map of the health check results
     */
    public SortedMap<String, HealthCheck.Result> runHealthChecks(ExecutorService executor) {
        final Map<String, Future<HealthCheck.Result>> futures = new HashMap<String, Future<Result>>();
        for (final Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            futures.put(entry.getKey(), executor.submit(new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    return entry.getValue().execute();
                }
            }));
        }

        final SortedMap<String, HealthCheck.Result> results = new TreeMap<String, HealthCheck.Result>();
        for (Map.Entry<String, Future<Result>> entry : futures.entrySet()) {
            try {
                results.put(entry.getKey(), entry.getValue().get());
            } catch (Exception e) {
                LOGGER.warn("Error executing health check {}", entry.getKey(), e);
            }
        }
        return Collections.unmodifiableSortedMap(results);
    }
}
