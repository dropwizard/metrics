package io.dropwizard.metrics.collectd.value;

public class GaugeTest extends DoubleValueTest<Gauge> {

	@Override
	protected Gauge createValue(final double initialMetric) {
		return new Gauge(initialMetric);
	}

	@Override
	protected DataType getExpectedDataType() {
		return DataType.GAUGE;
	}

}
