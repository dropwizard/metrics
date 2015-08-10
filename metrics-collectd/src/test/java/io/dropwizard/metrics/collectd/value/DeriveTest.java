package io.dropwizard.metrics.collectd.value;

public class DeriveTest extends LongValueTest<Derive> {

	@Override
	protected Derive createValue(final long initialMetric) {
		return new Derive(initialMetric);
	}

	@Override
	protected DataType getExpectedDataType() {
		return DataType.DERIVE;
	}

}
