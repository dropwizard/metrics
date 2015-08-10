package io.dropwizard.metrics.collectd.part;

public class Time extends NumericPart {
	public Time(final long value) {
		super(PartType.TIME, value);
	}
}
