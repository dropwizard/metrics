package com.codahale.metrics.health;

import com.codahale.metrics.MetricRegistry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A map of shared, named health registries.
 */
public class SharedHealthCheckRegistries {
    private static final ConcurrentMap<String, HealthCheckRegistry> REGISTRIES =
            new ConcurrentHashMap<String, HealthCheckRegistry>();

    private SharedHealthCheckRegistries() { /* singleton */ }

    public static void clear() {
        REGISTRIES.clear();
    }

    public static Set<String> names() {
        return REGISTRIES.keySet();
    }

    public static void remove(String key) {
        REGISTRIES.remove(key);
    }

    public static HealthCheckRegistry add(String name, HealthCheckRegistry registry) {
        return REGISTRIES.putIfAbsent(name, registry);
    }

    public static HealthCheckRegistry getOrCreate(String name) {
        final HealthCheckRegistry existing = REGISTRIES.get(name);
        if (existing == null) {
            final HealthCheckRegistry created = new HealthCheckRegistry();
            final HealthCheckRegistry raced = add(name, created);
            if (raced == null) {
                return created;
            }
            return raced;
        }
        return existing;
    }
}
