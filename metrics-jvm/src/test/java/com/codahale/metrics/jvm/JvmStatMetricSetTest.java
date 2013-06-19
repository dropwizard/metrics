package com.codahale.metrics.jvm;

import com.codahale.metrics.Metric;
import com.codahale.metrics.jvm.JvmStatMetricsSet;
import com.codahale.metrics.jvm.jvmstat.monitors.JvmStatsMetric;

import java.util.Map;

import org.fest.assertions.core.Condition;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class JvmStatMetricSetTest {
    private final JvmStatMetricsSet jvmStatMetricsSet = new JvmStatMetricsSet();

    @Test
    public void containAllMetrics() {
        Map<String,Metric> metrics = jvmStatMetricsSet.getMetrics();

        assertThat(metrics.keySet()).containsOnly("java.cls.loadedClasses", "sun.ci.totalCompiles");
    }

    @Test
    public void validateCorrectTypes() {
        Map<String,Metric> metrics = jvmStatMetricsSet.getMetrics();

        assertThat(metrics.values()).are(new Condition<Metric>() {
            @Override
            public boolean matches(Metric metric) {
                return metric instanceof JvmStatsMetric;
            }
        });
    }

    @Test
    public void validateCorrectValues() {
        Map<String,Metric> metrics = jvmStatMetricsSet.getMetrics();

        for (Metric metric : metrics.values()) {
            JvmStatsMetric<Long> jvmStatsMetric = (JvmStatsMetric<Long>) metric;

            assertThat(jvmStatsMetric.getValue()).isGreaterThan(0);
        }
    }
}
