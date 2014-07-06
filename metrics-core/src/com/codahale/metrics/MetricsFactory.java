package com.codahale.metrics;

import com.codahale.metrics.concrete.MetricRegistryConcrete;
import com.codahale.metrics.stub.MetricRegistryStub;

public class MetricsFactory {

	public static MetricRegistry getMetricRegistry(boolean enabled) {
		if (enabled) {
			return new MetricRegistryConcrete();
		} else {
			return new MetricRegistryStub();
		}
	}
	
}
