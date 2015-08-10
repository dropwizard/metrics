package io.dropwizard.metrics.collectd.part;

public class TimeHiResTest extends NumericPartTest<TimeHiRes> {
	@Override
	protected TimeHiRes createPart(final long initialMetric) {
		return new TimeHiRes(initialMetric);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.TIME_HR;
	}
}
