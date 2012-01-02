package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MetricsRegistryTest {
    @Test
    public void sortingMetricNamesSortsThemByClassThenScopeThenName() throws Exception {
        final MetricName one = new MetricName(Object.class, "one");
        final MetricName two = new MetricName(Object.class, "two");
        final MetricName three = new MetricName(String.class, "three");

        final MetricsRegistry registry = new MetricsRegistry();
        final Counter mOne = registry.newCounter(Object.class, "one");
        final Counter mTwo = registry.newCounter(Object.class, "two");
        final Counter mThree = registry.newCounter(String.class, "three");

        final SortedMap<String, SortedMap<MetricName, Metric>> sortedMetrics = new TreeMap<String, SortedMap<MetricName, Metric>>();
        final TreeMap<MetricName, Metric> objectMetrics = new TreeMap<MetricName, Metric>();
        objectMetrics.put(one, mOne);
        objectMetrics.put(two, mTwo);
        sortedMetrics.put(Object.class.getCanonicalName(), objectMetrics);

        final TreeMap<MetricName, Metric> stringMetrics = new TreeMap<MetricName, Metric>();
        stringMetrics.put(three, mThree);
        sortedMetrics.put(String.class.getCanonicalName(), stringMetrics);

        assertThat(registry.groupedMetrics(),
                   is(sortedMetrics));
    }

}
