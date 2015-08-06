package io.dropwizard.metrics.health;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A map of shared, named health registries.
 */
public class SharedHealthCheckRegistries {
    private static final ConcurrentMap<String, HealthCheckRegistry> REGISTRIES =
            new ConcurrentHashMap<String, HealthCheckRegistry>();

    private static final AtomicReference<HealthCheckRegistry> defaultRegistry = new AtomicReference<HealthCheckRegistry>();

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

    /**
     * @param registry the default registry
     * @throws IllegalStateException if the default has already been set
     */
    public static void setDefault(final HealthCheckRegistry registry) {
        if (defaultRegistry.compareAndSet(null, registry) == false) {
            throw new IllegalStateException("Default registry has already been set.");
        }
    }

    /**
     * @return the default registry
     * @throws IllegalStateException if the default has not been set
     */
    public static HealthCheckRegistry getDefault() {
        if (defaultRegistry.get() != null) {
            return defaultRegistry.get();
        }
        throw new IllegalStateException("Default registry has not been set.");
    }
}
