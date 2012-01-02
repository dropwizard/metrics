package com.yammer.metrics.util;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Utils {
    private Utils() { /* unused */ }

    public static Map<String, Map<MetricName, Metric>> sortMetrics(Map<MetricName, Metric> metrics) {
        return sortAndFilterMetrics(metrics, MetricPredicate.ALL);
    }

    public static Map<String, Map<MetricName, Metric>> sortAndFilterMetrics(Map<MetricName, Metric> metrics, MetricPredicate predicate) {
        final Map<String, Map<MetricName, Metric>> sortedMetrics = new TreeMap<String, Map<MetricName, Metric>>();
        for (Entry<MetricName, Metric> entry : metrics.entrySet()) {
            final String qualifiedTypeName = entry.getKey().getGroup() + "." + entry.getKey()
                                                                                    .getType();
            if (predicate.matches(entry.getKey(), entry.getValue())) {
                final String scopedName;
                if (entry.getKey().hasScope()) {
                    scopedName = qualifiedTypeName + "." + entry.getKey().getScope();
                } else {
                    scopedName = qualifiedTypeName;
                }
                Map<MetricName, Metric> subMetrics = sortedMetrics.get(scopedName);
                if (subMetrics == null) {
                    subMetrics = new TreeMap<MetricName, Metric>();
                    sortedMetrics.put(scopedName, subMetrics);
                }
                subMetrics.put(entry.getKey(), entry.getValue());
            }
        }
        return sortedMetrics;
    }
}
