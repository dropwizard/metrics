package com.codahale.metrics;

@SuppressWarnings("deprecation")
public interface MetricFilter {
	MetricFilter ALL = new MetricFilter() {
		@Override
        public boolean matches(String name, Metric metric) {
            return true;
        }
    };

    boolean matches(String name, Metric metric);
}
