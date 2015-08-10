package io.dropwizard.metrics.collectd.part;

public class Severity extends NumericPart {
	public Severity(final long value) {
		super(PartType.SEVERITY, value);
	}
}
