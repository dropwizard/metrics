package com.codahale.metrics.health;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A map of shared, named health registries.
 */
public class SharedHealthCheckRegistries {
    private static final ConcurrentMap<String, HealthCheckRegistry> REGISTRIES =
            new ConcurrentHashMap<>();

    private static AtomicReference<String> defaultRegistryName = new AtomicReference<>();

    /* Visible for testing */
    static void setDefaultRegistryName(AtomicReference<String> defaultRegistryName) {
        SharedHealthCheckRegistries.defaultRegistryName = defaultRegistryName;
    }

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
     * Creates a new registry and sets it as the default one under the provided name.
     *
     * @param name the registry name
     * @return the default registry
     * @throws IllegalStateException if the name has already been set
     */
    public synchronized static HealthCheckRegistry setDefault(String name) {
        final HealthCheckRegistry registry = getOrCreate(name);
        return setDefault(name, registry);
    }

    /**
     * Sets the provided registry as the default one under the provided name
     *
     * @param name                the default registry name
     * @param healthCheckRegistry the default registry
     * @throws IllegalStateException if the default registry has already been set
     */
    public static HealthCheckRegistry setDefault(String name, HealthCheckRegistry healthCheckRegistry) {
        if (defaultRegistryName.compareAndSet(null, name)) {
            add(name, healthCheckRegistry);
            return healthCheckRegistry;
        }
        throw new IllegalStateException("Default health check registry is already set.");
    }

    /**
     * Gets the name of the default registry, if it has been set
     *
     * @return the default registry
     * @throws IllegalStateException if the default has not been set
     */
    public static HealthCheckRegistry getDefault() {
        final HealthCheckRegistry healthCheckRegistry = tryGetDefault();
        if (healthCheckRegistry != null) {
            return healthCheckRegistry;
        }
        throw new IllegalStateException("Default registry name has not been set.");
    }

    /**
     * Same as {@link #getDefault()} except returns null when the default registry has not been set.
     *
     * @return the default registry or null
     */
    public static HealthCheckRegistry tryGetDefault() {
        final String name = defaultRegistryName.get();
        if (name != null) {
            return getOrCreate(name);
        }
        return null;
    }
}
