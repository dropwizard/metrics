package io.dropwizard.metrics.collectd.value;

public class CounterTest extends LongValueTest<Counter> {

	@Override
	protected Counter createValue(final long initialMetric) {
		return new Counter(initialMetric);
	}

	@Override
	protected DataType getExpectedDataType() {
		return DataType.COUNTER;
	}

}
