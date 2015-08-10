package io.dropwizard.metrics.collectd.part;

public class IntervalHiResTest extends NumericPartTest<IntervalHiRes> {
	@Override
	protected IntervalHiRes createPart(final long initialMetric) {
		return new IntervalHiRes(initialMetric);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.INTERVAL_HR;
	}
}
