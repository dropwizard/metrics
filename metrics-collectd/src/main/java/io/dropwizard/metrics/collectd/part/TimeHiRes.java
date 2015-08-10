package io.dropwizard.metrics.collectd.part;

public class TimeHiRes extends NumericPart {
	public TimeHiRes(final long value) {
		super(PartType.TIME_HR, value);
	}
}
