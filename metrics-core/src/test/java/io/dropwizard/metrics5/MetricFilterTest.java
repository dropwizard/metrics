package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MetricFilterTest {
    @Test
    void theAllFilterMatchesAllMetrics() {
        assertThat(MetricFilter.ALL.matches(MetricName.build(""), mock(Metric.class)))
                .isTrue();
    }

    @Test
    void theStartsWithFilterMatches() {
        assertThat(MetricFilter.startsWith("foo").matches(MetricName.build("foo.bar"), mock(Metric.class)))
                .isTrue();
        assertThat(MetricFilter.startsWith("foo").matches(MetricName.build("bar.foo"), mock(Metric.class)))
                .isFalse();
    }

    @Test
    void theEndsWithFilterMatches() {
        assertThat(MetricFilter.endsWith("foo").matches(MetricName.build("foo.bar"), mock(Metric.class)))
                .isFalse();
        assertThat(MetricFilter.endsWith("foo").matches(MetricName.build("bar.foo"), mock(Metric.class)))
                .isTrue();
    }

    @Test
    void theContainsFilterMatches() {
        assertThat(MetricFilter.contains("foo").matches(MetricName.build("bar.foo.bar"), mock(Metric.class)))
                .isTrue();
        assertThat(MetricFilter.contains("foo").matches(MetricName.build("bar.bar"), mock(Metric.class)))
                .isFalse();
    }
}
