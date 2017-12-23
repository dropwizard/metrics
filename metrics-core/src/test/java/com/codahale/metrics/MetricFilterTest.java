package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MetricFilterTest {
    @Test
    public void theAllFilterMatchesAllMetrics() {
        assertThat(MetricFilter.ALL.matches("", mock(Metric.class)))
                .isTrue();
    }

    @Test
    public void theStartsWithFilterMatches() {
        assertThat(MetricFilter.startsWith("foo").matches("foo.bar", mock(Metric.class)))
                .isTrue();
        assertThat(MetricFilter.startsWith("foo").matches("bar.foo", mock(Metric.class)))
                .isFalse();
    }

    @Test
    public void theEndsWithFilterMatches() {
        assertThat(MetricFilter.endsWith("foo").matches("foo.bar", mock(Metric.class)))
                .isFalse();
        assertThat(MetricFilter.endsWith("foo").matches("bar.foo", mock(Metric.class)))
                .isTrue();
    }

    @Test
    public void theContainsFilterMatches() {
        assertThat(MetricFilter.contains("foo").matches("bar.foo.bar", mock(Metric.class)))
                .isTrue();
        assertThat(MetricFilter.contains("foo").matches("bar.bar", mock(Metric.class)))
                .isFalse();
    }
}
