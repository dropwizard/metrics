package com.codahale.metrics;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Deprecated
public class SharedMetricRegistries {

    public static void clear() {
        io.dropwizard.metrics5.SharedMetricRegistries.clear();
    }

    public static Set<String> names() {
        return io.dropwizard.metrics5.SharedMetricRegistries.names();
    }

    public static void remove(String key) {
        io.dropwizard.metrics5.SharedMetricRegistries.remove(key);
    }

    public static MetricRegistry add(String name, MetricRegistry registry) {
        io.dropwizard.metrics5.SharedMetricRegistries.add(name, registry.getDelegate());
        return registry;
    }

    public static MetricRegistry getOrCreate(String name) {
        return new MetricRegistry(io.dropwizard.metrics5.SharedMetricRegistries.getOrCreate(name));
    }

    public synchronized static MetricRegistry setDefault(String name) {
        return new MetricRegistry(io.dropwizard.metrics5.SharedMetricRegistries.setDefault(name));
    }

    public static MetricRegistry setDefault(String name, MetricRegistry metricRegistry) {
        io.dropwizard.metrics5.SharedMetricRegistries.setDefault(name, metricRegistry.getDelegate());
        return metricRegistry;
    }

    public static MetricRegistry getDefault() {
        return new MetricRegistry(io.dropwizard.metrics5.SharedMetricRegistries.getDefault());
    }

    public static MetricRegistry tryGetDefault() {
        return Optional.ofNullable(io.dropwizard.metrics5.SharedMetricRegistries.tryGetDefault())
                .map(MetricRegistry::new)
                .orElse(null);
    }
}
