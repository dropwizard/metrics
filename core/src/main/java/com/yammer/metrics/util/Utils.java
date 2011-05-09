package com.yammer.metrics.util;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Utils {
    private Utils() { /* unused */ }

    public static Map<String, Map<String, Metric>> sortMetrics(Map<MetricName, Metric> metrics) {
        final Map<String, Map<String, Metric>> sortedMetrics =
                new TreeMap<String, Map<String, Metric>>();
        for (Entry<MetricName, Metric> entry : metrics.entrySet()) {
            final String className = entry.getKey().getKlass()
                                          .getCanonicalName()
                                          .replace('$', '.')
                                          .replaceAll("\\.$", "");
            final String scopedName;
            if (entry.getKey().hasScope()) {
                scopedName = className + "." + entry.getKey().getScope();
            } else {
                scopedName = className;
            }
            Map<String, Metric> submetrics = sortedMetrics.get(scopedName);
            if (submetrics == null) {
                submetrics = new TreeMap<String, Metric>();
                sortedMetrics.put(scopedName, submetrics);
            }
            submetrics.put(entry.getKey().getName(), entry.getValue());
        }
        return sortedMetrics;
    }
}
