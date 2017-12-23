package com.codahale.metrics.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class HealthCheckFilterTest {

    @Test
    public void theAllFilterMatchesAllHealthChecks() {
        assertThat(HealthCheckFilter.ALL.matches("", mock(HealthCheck.class))).isTrue();
    }
}
