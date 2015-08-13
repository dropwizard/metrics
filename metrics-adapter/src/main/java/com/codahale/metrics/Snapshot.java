package com.codahale.metrics;

import java.io.OutputStream;

@Deprecated
public class Snapshot extends io.dropwizard.metrics.Snapshot {
	final io.dropwizard.metrics.Snapshot snap;
	
	public Snapshot(io.dropwizard.metrics.Snapshot snap){
		this.snap = snap;
	}

	@Override
	public double getValue(double quantile) {
		return snap.getValue(quantile);
	}

	@Override
	public long[] getValues() {
		return snap.getValues();
	}

	@Override
	public int size() {
		return snap.size();
	}

	@Override
	public long getMax() {
		return snap.getMax();
	}

	@Override
	public double getMean() {
		return snap.getMean();
	}

	@Override
	public long getMin() {
		return snap.getMin();
	}

	@Override
	public double getStdDev() {
		return snap.getStdDev();
	}

	@Override
	public void dump(OutputStream output) {
		snap.dump(output);
	}

}
