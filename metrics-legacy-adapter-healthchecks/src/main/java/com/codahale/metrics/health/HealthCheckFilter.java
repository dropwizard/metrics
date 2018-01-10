package com.codahale.metrics.health;

@Deprecated
public interface HealthCheckFilter {

    HealthCheckFilter ALL = (name, healthCheck) -> true;

    boolean matches(String name, HealthCheck healthCheck);

    default io.dropwizard.metrics5.health.HealthCheckFilter transform() {
        final HealthCheckFilter origin = this;
        return (name, healthCheck) -> origin.matches(name, HealthCheck.of(healthCheck));
    }
}
