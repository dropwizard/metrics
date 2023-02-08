package io.dropwizard.metrics5.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class HealthCheckFilterTest {

    @Test
    void theAllFilterMatchesAllHealthChecks() {
        assertThat(HealthCheckFilter.ALL.matches("", mock(HealthCheck.class))).isTrue();
    }
}
