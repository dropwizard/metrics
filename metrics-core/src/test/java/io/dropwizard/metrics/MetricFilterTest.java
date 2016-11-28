package io.dropwizard.metrics;

import org.junit.Test;

import io.dropwizard.metrics.Metric;
import io.dropwizard.metrics.MetricFilter;
import io.dropwizard.metrics.MetricName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MetricFilterTest {
    @Test
    public void theAllFilterMatchesAllMetrics() throws Exception {
        assertThat(MetricFilter.ALL.matches(MetricName.build(""), mock(Metric.class)))
                .isTrue();
    }
}
