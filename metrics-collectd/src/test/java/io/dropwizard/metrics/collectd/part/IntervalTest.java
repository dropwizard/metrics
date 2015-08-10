package io.dropwizard.metrics.collectd.part;

public class IntervalTest extends NumericPartTest<Interval> {
	@Override
	protected Interval createPart(final long initialMetric) {
		return new Interval(initialMetric);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.INTERVAL;
	}
}
