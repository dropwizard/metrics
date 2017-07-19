package com.codahale.metrics.health;

/**
 * A filter used to determine whether or not a health check should be reported.
 */
public interface HealthCheckFilter {
    /**
     * Matches all health checks, regardless of type or name.
     */
    HealthCheckFilter ALL = new HealthCheckFilter() {
        @Override
        public boolean matches(String name, HealthCheck healthCheck) {
            return true;
        }
    };

    /**
     * Returns {@code true} if the health check matches the filter; {@code false} otherwise.
     *
     * @param name        the health check's name
     * @param healthCheck the health check
     * @return {@code true} if the health check matches the filter
     */
    boolean matches(String name, HealthCheck healthCheck);
}
