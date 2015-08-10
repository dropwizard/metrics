package io.dropwizard.metrics.collectd.value;

public class AbsoluteTest extends LongValueTest<Absolute> {

	@Override
	protected Absolute createValue(final long initialMetric) {
		return new Absolute(initialMetric);
	}

	@Override
	protected DataType getExpectedDataType() {
		return DataType.ABSOLUTE;
	}

}
