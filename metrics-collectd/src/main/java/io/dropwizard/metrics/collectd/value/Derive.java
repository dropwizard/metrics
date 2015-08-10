package io.dropwizard.metrics.collectd.value;

public class Derive extends LongValue {
	public Derive(final long value) {
		super(DataType.DERIVE, value);
	}
}
