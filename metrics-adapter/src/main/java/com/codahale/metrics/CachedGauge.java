package com.codahale.metrics;

import io.dropwizard.metrics.Clock;

import java.util.concurrent.TimeUnit;

@Deprecated
public abstract class CachedGauge<T> extends io.dropwizard.metrics.CachedGauge<T> implements Metric {
	protected CachedGauge(long timeout, TimeUnit timeoutUnit) {
		super(timeout, timeoutUnit);
	}

	protected CachedGauge(Clock clock, long timeout, TimeUnit timeoutUnit) {
		super(clock, timeout, timeoutUnit);
	}
}
