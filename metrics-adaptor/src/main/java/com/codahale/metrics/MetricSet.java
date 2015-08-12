package com.codahale.metrics;

import java.util.Map;

@Deprecated
public interface MetricSet {
	Map<String, Metric> getMetrics();
}
