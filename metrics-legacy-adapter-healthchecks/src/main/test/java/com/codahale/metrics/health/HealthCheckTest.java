package com.codahale.metrics.health;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@SuppressWarnings("deprecation")
public class HealthCheckTest {

    @Test
    public void testCreateHealthyCheck() {
        HealthCheck healthCheck = new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        };
        assertThat(healthCheck.execute().isHealthy()).isTrue();
    }

    @Test
    public void testCreateHealthyCheckWithMessage() {
        HealthCheck healthCheck = new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy("DB is up");
            }
        };
        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy()).isTrue();
        assertThat(result.getMessage()).isEqualTo("DB is up");
    }

    @Test
    public void testCreateHealthyCheckWithDynamicMessage() {
        HealthCheck healthCheck = new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy("DB %s is up", "se-us-1");
            }
        };
        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy()).isTrue();
        assertThat(result.getMessage()).isEqualTo("DB se-us-1 is up");
    }

    @Test
    public void testCreateUnhealthyCheck() {
        HealthCheck healthCheck = new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.unhealthy("DB is down");
            }
        };
        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy()).isFalse();
        assertThat(result.getMessage()).isEqualTo("DB is down");
    }

    @Test
    public void testCreateUnhealthyCheckWithDynamicMessage() {
        HealthCheck healthCheck = new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.unhealthy("DB %s is down", "se-us-1");
            }
        };
        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy()).isFalse();
        assertThat(result.getMessage()).isEqualTo("DB se-us-1 is down");
    }

    @Test
    public void testThrowExceptionInHealthCheck() {
        HealthCheck healthCheck = new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                throw new IllegalStateException("DB is down");
            }
        };
        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy()).isFalse();
        assertThat(result.getMessage()).isEqualTo("DB is down");
        assertThat(result.getError()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testBuildCustomResult() {
        HealthCheck healthCheck = new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.builder()
                        .healthy()
                        .withMessage("DB is up")
                        .withDetail("responseTime", 5)
                        .build();
            }
        };

        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy()).isTrue();
        assertThat(result.getMessage()).isEqualTo("DB is up");
        assertThat(result.getDetails()).containsExactly(entry("responseTime", 5));
    }


    @Test
    public void testBuildCustomUnhealthyResult() {
        HealthCheck healthCheck = new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.builder()
                        .unhealthy()
                        .withMessage("DB %s is down", "se-us-1")
                        .withDetail("retries", 3)
                        .build();
            }
        };

        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy()).isFalse();
        assertThat(result.getMessage()).isEqualTo("DB se-us-1 is down");
        assertThat(result.getDetails()).containsExactly(entry("retries", 3));
    }
}
