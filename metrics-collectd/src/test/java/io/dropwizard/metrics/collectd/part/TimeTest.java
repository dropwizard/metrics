package io.dropwizard.metrics.collectd.part;

public class TimeTest extends NumericPartTest<Time> {
	@Override
	protected Time createPart(final long initialMetric) {
		return new Time(initialMetric);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.TIME;
	}
}
