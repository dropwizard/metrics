package io.dropwizard.metrics.collectd.value;

public class Counter extends LongValue {
	public Counter(final long value) {
		super(DataType.COUNTER, value);
	}
}
