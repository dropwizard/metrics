package com.codahale.metrics.health;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class HealthCheckFilterTest {

    @Test
    public void theAllFilterMatchesAllHealthChecks() {
        assertThat(HealthCheckFilter.ALL.matches("", mock(HealthCheck.class))).isTrue();
    }
}
