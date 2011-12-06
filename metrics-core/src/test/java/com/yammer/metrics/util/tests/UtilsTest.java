package com.yammer.metrics.util.tests;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.util.Utils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class UtilsTest {
    @Test
    public void sortingMetricNamesSortsThemByClassThenScopeThenName() throws Exception {
        final Map<MetricName, Metric> metrics = new HashMap<MetricName, Metric>();

        final MetricName one = new MetricName(Object.class, "one");
        final MetricName two = new MetricName(Object.class, "two");
        final MetricName three = new MetricName(String.class, "three");

        final Metric mOne = mock(Metric.class);
        final Metric mTwo = mock(Metric.class);
        final Metric mThree = mock(Metric.class);
        metrics.put(one, mOne);
        metrics.put(two, mTwo);
        metrics.put(three, mThree);

        final Map<String, Map<MetricName, Metric>> sortedMetrics = new TreeMap<String, Map<MetricName, Metric>>();
        final TreeMap<MetricName, Metric> objectMetrics = new TreeMap<MetricName, Metric>();
        objectMetrics.put(one, mOne);
        objectMetrics.put(two, mTwo);
        sortedMetrics.put(Object.class.getCanonicalName(), objectMetrics);

        final TreeMap<MetricName, Metric> stringMetrics = new TreeMap<MetricName, Metric>();
        stringMetrics.put(three, mThree);
        sortedMetrics.put(String.class.getCanonicalName(), stringMetrics);

        assertThat(Utils.sortMetrics(metrics),
                   is(sortedMetrics));
    }
}
