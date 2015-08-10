package io.dropwizard.metrics.collectd.part;

public class Interval extends NumericPart {
	public Interval(final long value) {
		super(PartType.INTERVAL, value);
	}
}
