package com.yammer.metrics.core;


/**
 * Listeners for events from the registry.  Listeners must be thread-safe.
 */
public interface HealthCheckRegistryListener {
    /**
     * Called when a {@link HealthCheck} has been added to the {@link HealthCheckRegistry}.
     *
     * @param check the {@link HealthCheck}
     */
    public void onHealthCheckAdded(HealthCheck check);

    /**
     * Called when a {@link HealthCheck} has been removed from the {@link HealthCheckRegistry}.
     *
     * @param name the name of the {@link HealthCheck}
     *
     */
    public void onHealthCheckRemoved(MetricName name);
}
