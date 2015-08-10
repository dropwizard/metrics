package io.dropwizard.metrics.collectd.value;

public class Absolute extends LongValue {
	public Absolute(final long value) {
		super(DataType.ABSOLUTE, value);
	}
}
