package com.yammer.metrics.tests;

import com.yammer.metrics.Metric;
import com.yammer.metrics.MetricFilter;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

public class MetricFilterTest {
    @Test
    public void theAllFilterMatchesAllMetrics() throws Exception {
        assertThat(MetricFilter.ALL.matches(anyString(), any(Metric.class)))
                .isTrue();
    }
}
