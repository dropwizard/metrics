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

    @Test
    public void theStartsWithFilterMatches() throws Exception {
        assertThat(MetricFilter.startsWith("foo").matches(MetricName.build("foo.bar"), mock(Metric.class)))
                .isTrue();
        assertThat(MetricFilter.startsWith("foo").matches(MetricName.build("bar.foo"), mock(Metric.class)))
                .isFalse();
    }

    @Test
    public void theEndsWithFilterMatches() throws Exception {
        assertThat(MetricFilter.endsWith("foo").matches(MetricName.build("foo.bar"), mock(Metric.class)))
                .isFalse();
        assertThat(MetricFilter.endsWith("foo").matches(MetricName.build("bar.foo"), mock(Metric.class)))
                .isTrue();
    }

    @Test
    public void theContainsFilterMatches() throws Exception {
        assertThat(MetricFilter.contains("foo").matches(MetricName.build("bar.foo.bar"), mock(Metric.class)))
                .isTrue();
        assertThat(MetricFilter.contains("foo").matches(MetricName.build("bar.bar"), mock(Metric.class)))
                .isFalse();
    }
}
