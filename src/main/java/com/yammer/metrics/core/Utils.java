package com.yammer.metrics.core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

class Utils {
	private Utils() { /* unused */ }

	static Map<String, Map<String, Metric>> sortMetrics(Map<MetricName, Metric> metrics) {
		final Map<String, Map<String, Metric>> sortedMetrics =
				new TreeMap<String, Map<String, Metric>>();
		for (Entry<MetricName, Metric> entry : metrics.entrySet()) {
			final String className = entry.getKey().getKlass()
										  .getCanonicalName()
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
