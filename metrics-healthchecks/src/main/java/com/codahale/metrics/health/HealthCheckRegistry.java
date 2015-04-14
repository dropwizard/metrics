package com.codahale.metrics.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static com.codahale.metrics.health.HealthCheck.Result;

/**
 * A registry for health checks.
 */
public class HealthCheckRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckRegistry.class);

    private final ConcurrentMap<String, HealthCheck> healthChecks;
    private final List<HealthCheckRegistryListener> listeners;

    /**
     * Creates a new {@link HealthCheckRegistry}.
     */
    public HealthCheckRegistry() {
        this.healthChecks = new ConcurrentHashMap<String, HealthCheck>();
        this.listeners = new CopyOnWriteArrayList<HealthCheckRegistryListener>();
    }

    /**
     * Adds a {@link HealthCheckRegistryListener} to a collection of listeners
     * that will be notified on health check registration. Listeners will be
     * notified in the order in which they are added.
     * The listener will be notified of all existing health checks when it first registers.
     * 
     * @param listener listener to add
     */
    public void addListener(HealthCheckRegistryListener listener) {
        listeners.add(listener);
        for (Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            listener.onHealthCheckAdded(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes a {@link HealthCheckRegistryListener} from this registry's
     * collection of listeners.
     * 
     * @param listener listener to remove
     */
    public void removeListener(HealthCheckRegistryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Registers an application {@link HealthCheck}.
     *
     * @param name        the name of the health check
     * @param healthCheck the {@link HealthCheck} instance
     */
    public void register(String name, HealthCheck healthCheck) {
        HealthCheck existing = healthChecks.putIfAbsent(name, healthCheck);
        if (existing == null) {
            onHealthCheckAdded(name, healthCheck);
        }
    }

    /**
     * Unregisters the application {@link HealthCheck} with the given name.
     *
     * @param name the name of the {@link HealthCheck} instance
     */
    public void unregister(String name) {
        healthChecks.remove(name);
        onHealthCheckRemoved(name);
    }

    /**
     * Returns a set of the names of all registered health checks.
     *
     * @return the names of all registered health checks
     */
    public SortedSet<String> getNames() {
        return Collections.unmodifiableSortedSet(new TreeSet<String>(healthChecks.keySet()));
    }

    /**
     * Runs the health check with the given name.
     *
     * @param name    the health check's name
     * @return the result of the health check
     * @throws NoSuchElementException if there is no health check with the given name
     */
    public HealthCheck.Result runHealthCheck(String name) throws NoSuchElementException {
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
     * @param   executor object to launch and track health checks progress
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
                results.put(entry.getKey(), HealthCheck.Result.unhealthy(e));
            }
        }
        return Collections.unmodifiableSortedMap(results);
    }

    private void onHealthCheckAdded(String name, HealthCheck healthCheck) {
        for (HealthCheckRegistryListener listener : listeners) {
            listener.onHealthCheckAdded(name, healthCheck);
        }
    }

    private void onHealthCheckRemoved(String name) {
        for (HealthCheckRegistryListener listener : listeners) {
            listener.onHealthCheckRemoved(name);
        }
    }

}
