package com.codahale.metrics.health;

import java.util.EventListener;
import java.util.Objects;

@Deprecated
public interface HealthCheckRegistryListener extends EventListener {

    void onHealthCheckAdded(String name, HealthCheck healthCheck);

    void onHealthCheckRemoved(String name, HealthCheck healthCheck);

    default io.dropwizard.metrics5.health.HealthCheckRegistryListener transform() {
        return new Adapter(this);
    }

    class Adapter implements io.dropwizard.metrics5.health.HealthCheckRegistryListener {

        private final HealthCheckRegistryListener delegate;

        public Adapter(HealthCheckRegistryListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onHealthCheckAdded(String name, io.dropwizard.metrics5.health.HealthCheck healthCheck) {
            delegate.onHealthCheckAdded(name, HealthCheck.of(healthCheck));
        }

        @Override
        public void onHealthCheckRemoved(String name, io.dropwizard.metrics5.health.HealthCheck healthCheck) {
            delegate.onHealthCheckRemoved(name, HealthCheck.of(healthCheck));
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Adapter) {
                final Adapter that = (Adapter) o;
                return Objects.equals(delegate, that.delegate);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(delegate);
        }
    }
}
