package com.codahale.metrics;

import java.util.Map;

public interface MetricSet {
	Map<String, Metric> getMetrics();
}
