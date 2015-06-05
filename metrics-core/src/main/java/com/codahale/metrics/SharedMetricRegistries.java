package com.codahale.metrics;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A map of shared, named metric registries.
 */
public class SharedMetricRegistries {
    private static final ConcurrentMap<String, MetricRegistry> REGISTRIES =
            new ConcurrentHashMap<String, MetricRegistry>();

    private static volatile String defaultRegistryName = null;

    private SharedMetricRegistries() { /* singleton */ }

    public static void clear() {
        REGISTRIES.clear();
    }

    public static Set<String> names() {
        return REGISTRIES.keySet();
    }

    public static void remove(String key) {
        REGISTRIES.remove(key);
    }

    public static MetricRegistry add(String name, MetricRegistry registry) {
        return REGISTRIES.putIfAbsent(name, registry);
    }

    public static MetricRegistry getOrCreate(String name) {
        final MetricRegistry existing = REGISTRIES.get(name);
        if (existing == null) {
            final MetricRegistry created = new MetricRegistry();
            final MetricRegistry raced = add(name, created);
            if (raced == null) {
                return created;
            }
            return raced;
        }
        return existing;
    }

    public synchronized static MetricRegistry setDefault(String name) {
        final MetricRegistry registry = getOrCreate(name);
        return setDefault(name, registry);
    }

    public static MetricRegistry setDefault(String name, MetricRegistry metricRegistry) {
        if (defaultRegistryName == null) {
            defaultRegistryName = name;
            add(name, metricRegistry);
            return metricRegistry;
        }
        throw new IllegalStateException("Default metric registry name is already set.");
    }

    public static MetricRegistry getDefault() {
        if (defaultRegistryName != null) {
            return getOrCreate(defaultRegistryName);
        }
        throw new IllegalStateException("Default registry name has not been set.");
    }
}
