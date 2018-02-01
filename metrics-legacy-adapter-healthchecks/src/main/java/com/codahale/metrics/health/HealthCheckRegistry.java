package com.codahale.metrics.health;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

@Deprecated
public class HealthCheckRegistry {

    private final io.dropwizard.metrics5.health.HealthCheckRegistry delegate;

    public HealthCheckRegistry() {
        this(new io.dropwizard.metrics5.health.HealthCheckRegistry());
    }

    public HealthCheckRegistry(int asyncExecutorPoolSize) {
        this(new io.dropwizard.metrics5.health.HealthCheckRegistry(asyncExecutorPoolSize));
    }

    public HealthCheckRegistry(ScheduledExecutorService asyncExecutorService) {
        this(new io.dropwizard.metrics5.health.HealthCheckRegistry(asyncExecutorService));
    }

    private HealthCheckRegistry(io.dropwizard.metrics5.health.HealthCheckRegistry delegate) {
        this.delegate = delegate;
    }

    public static HealthCheckRegistry of(io.dropwizard.metrics5.health.HealthCheckRegistry reg) {
        return new HealthCheckRegistry(reg);
    }

    public void addListener(HealthCheckRegistryListener listener) {
        delegate.addListener(listener.transform());
    }

    public void removeListener(HealthCheckRegistryListener listener) {
        delegate.removeListener(listener.transform());
    }

    public void register(String name, HealthCheck healthCheck) {
        delegate.register(name, healthCheck.transform());
    }

    public void unregister(String name) {
        delegate.unregister(name);
    }

    public SortedSet<String> getNames() {
        return delegate.getNames();
    }

    public HealthCheck.Result runHealthCheck(String name) throws NoSuchElementException {
        return HealthCheck.Result.of(delegate.runHealthCheck(name));
    }

    public SortedMap<String, HealthCheck.Result> runHealthChecks() {
        return convertHealthChecks(delegate.runHealthChecks());
    }

    public SortedMap<String, HealthCheck.Result> runHealthChecks(HealthCheckFilter filter) {
        return convertHealthChecks(delegate.runHealthChecks(filter.transform()));
    }

    public SortedMap<String, HealthCheck.Result> runHealthChecks(ExecutorService executor) {
        return convertHealthChecks(delegate.runHealthChecks(executor));
    }

    public SortedMap<String, HealthCheck.Result> runHealthChecks(ExecutorService executor,
                                                                 HealthCheckFilter filter) {
        return convertHealthChecks(delegate.runHealthChecks(executor, filter.transform()));
    }

    private SortedMap<String, HealthCheck.Result> convertHealthChecks(
            SortedMap<String, io.dropwizard.metrics5.health.HealthCheck.Result> originResults) {
        final SortedMap<String, HealthCheck.Result> results = new TreeMap<>();
        for (Map.Entry<String, io.dropwizard.metrics5.health.HealthCheck.Result> entry : originResults.entrySet()) {
            results.put(entry.getKey(), HealthCheck.Result.of(entry.getValue()));
        }
        return results;
    }

    public void shutdown() {
        delegate.shutdown();
    }
}
