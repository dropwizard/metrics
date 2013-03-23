package com.yammer.metrics.tests;

import com.yammer.metrics.Metric;
import com.yammer.metrics.MetricFilter;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MetricFilterTest {
    @Test
    public void theAllFilterMatchesAllMetrics() throws Exception {
        assertThat(MetricFilter.ALL.matches("", mock(Metric.class)))
                .isTrue();
    }
}
