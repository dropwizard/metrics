package io.dropwizard.metrics.collectd.value;

public class Gauge extends DoubleValue {
	public Gauge(final double value) {
		super(DataType.GAUGE, value);
	}
}
