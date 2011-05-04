package com.yammer.metrics.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;

public class Utils {
	private Utils() { /* unused */ }

	public static Map<String, Map<String, Metric>> sortMetrics(Map<MetricName, Metric> metrics) {
		final Map<String, Map<String, Metric>> sortedMetrics =
				new TreeMap<String, Map<String, Metric>>();
		for (Entry<MetricName, Metric> entry : metrics.entrySet()) {
		  final MetricName mname = entry.getKey();
			final String className = (mname.getDomain() + "." + mname.getType())
			  .replace('$', '.')
			  .replaceAll("\\.$", "");
			Map<String, Metric> submetrics = sortedMetrics.get(className);
			if (submetrics == null) {
				submetrics = new TreeMap<String, Metric>();
				sortedMetrics.put(className, submetrics);
			}
			submetrics.put(entry.getKey().getName(), entry.getValue());
		}
		return sortedMetrics;
	}
}
