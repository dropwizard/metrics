package io.dropwizard.metrics.collectd.part;

public class IntervalHiRes extends NumericPart {
	public IntervalHiRes(final long value) {
		super(PartType.INTERVAL_HR, value);
	}
}
