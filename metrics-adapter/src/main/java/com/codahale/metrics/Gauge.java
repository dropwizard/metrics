package com.codahale.metrics;

@Deprecated
public class Gauge<T> implements io.dropwizard.metrics.Gauge<T>, Metric {
	final io.dropwizard.metrics.Gauge<T> gauge;
	
	public Gauge(io.dropwizard.metrics.Gauge<T> gauge){
		this.gauge = gauge;
	}

	@Override
	public T getValue() {
		return gauge.getValue();
	}
}
