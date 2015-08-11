package com.codahale.metrics;

import io.dropwizard.metrics.Counting;

public class Counter implements Metric, Counting {
	final io.dropwizard.metrics.Counter counter;

	public Counter(io.dropwizard.metrics.Counter counter) {
		this.counter = counter;
	}

	public void inc() {
		counter.inc(1);
	}

	public void inc(long n) {
		counter.inc(n);
	}

	public void dec() {
		counter.dec(1);
	}

	public void dec(long n) {
		counter.dec(-n);
	}

	@Override
	public long getCount() {
		return counter.getCount();
	}
}
