package com.yammer.metrics.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.yammer.metrics.core.HealthCheck.Result;

/**
 * A registry for health checks.
 */
public class HealthCheckRegistry {
    private final ConcurrentMap<MetricName, HealthCheck> checks = newHealthCheckMap();
    private final List<HealthCheckRegistryListener> listeners =
            new CopyOnWriteArrayList<HealthCheckRegistryListener>();

    /**
     * Registers an application {@link HealthCheck}.
     *
     * @param healthCheck the {@link HealthCheck} instance
     */
    public void register(HealthCheck healthCheck) {
        checks.putIfAbsent(healthCheck.name(), healthCheck);
        notifyCheckAdded(healthCheck.name(), healthCheck);
    }

    /**
     * Unregisters the application {@link HealthCheck} with the given name.
     *
     * @param name the name of the {@link HealthCheck} instance
     */
    public void unregister(MetricName name) {
        checks.remove(name);
        notifyCheckRemoved(name);
    }

    /**
     * Unregisters the given {@link HealthCheck}.
     *
     * @param healthCheck    a {@link HealthCheck}
     */
    public void unregister(HealthCheck healthCheck) {
        unregister(healthCheck.name());
    }

    /**
     * Runs the registered health checks and returns a map of the results.
     *
     * @return a map of the health check results
     */
    public SortedMap<MetricName, Result> runHealthChecks() {
        final SortedMap<MetricName, Result> results = new TreeMap<MetricName, Result>();
        for (Entry<MetricName, HealthCheck> entry : checks.entrySet()) {
            final Result result = entry.getValue().execute();
            results.put(entry.getKey(), result);
        }
        return Collections.unmodifiableSortedMap(results);
    }
    
    /**
     * Returns a new {@link ConcurrentMap} implementation. Subclass this to do
     * weird things with your own {@link HealthCheckRegistry} implementation.
     *
     * @return a new {@link ConcurrentMap}
     */
    protected ConcurrentMap<MetricName, HealthCheck> newHealthCheckMap() {
        return new ConcurrentHashMap<MetricName, HealthCheck>();
    }


    /**
     * Adds a {@link HealthCheckRegistryListener} to a collection of listeners that will be notified on
     * {@link HealthCheck} creation. Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing {@link HealthCheck}s when it first registers.
     *
     * @param listener the listener that will be notified
     */
    public void addListener(HealthCheckRegistryListener listener) {
        listeners.add(listener);
        for (Map.Entry<MetricName, HealthCheck> entry : checks.entrySet()) {
            listener.onHealthCheckAdded(entry.getValue());
        }
    }

    /**
     * Removes a {@link HealthCheckRegistryListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeListener(HealthCheckRegistryListener listener) {
        listeners.remove(listener);
    }

    private void notifyCheckRemoved(MetricName name) {
        for (HealthCheckRegistryListener listener : listeners) {
            listener.onHealthCheckRemoved(name);
        }
    }

    private void notifyCheckAdded(MetricName name, HealthCheck check) {
        for (HealthCheckRegistryListener listener : listeners) {
            listener.onHealthCheckAdded(check);
        }
    }
}
