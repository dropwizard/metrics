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
}
