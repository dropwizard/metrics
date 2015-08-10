package io.dropwizard.metrics.collectd.part;

public class SeverityTest extends NumericPartTest<Severity> {
	@Override
	protected Severity createPart(final long initialMetric) {
		return new Severity(initialMetric);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.SEVERITY;
	}
}
