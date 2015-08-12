package com.codahale.metrics;

import io.dropwizard.metrics.Counting;
import io.dropwizard.metrics.Sampling;
import io.dropwizard.metrics.Snapshot;

@Deprecated
public class Histogram implements Metric, Sampling, Counting {
	final io.dropwizard.metrics.Histogram hist;

	public Histogram(io.dropwizard.metrics.Histogram hist) {
		this.hist = hist;
	}

	public void update(int value) {
		hist.update(value);
	}

	public void update(long value) {
		hist.update(value);
	}

	@Override
	public long getCount() {
		return hist.getCount();
	}

	@Override
	public Snapshot getSnapshot() {
		return hist.getSnapshot();
	}
}
