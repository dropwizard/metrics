package com.codahale.metrics.health;

import java.util.EventListener;

/**
 * A listener contract for {@link HealthCheckRegistry} events.
 */
public interface HealthCheckRegistryListener extends EventListener {

    /**
     * Called when a new {@link HealthCheck} is added to the registry.
     *
     * @param name        the name of the health check
     * @param healthCheck the health check
     */
    void onHealthCheckAdded(String name, HealthCheck healthCheck);

    /**
     * Called when a {@link HealthCheck} is removed from the registry.
     *
     * @param name        the name of the health check
     * @param healthCheck the health check
     */
    void onHealthCheckRemoved(String name, HealthCheck healthCheck);

}
