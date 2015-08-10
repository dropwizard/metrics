package io.dropwizard.metrics.collectd.value;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

public abstract class DoubleValueTest<D extends DoubleValue> extends ValueTest<D> {

	private double initialMetric;

	@Test
	public void shouldEncodeTheInitialValueAsBytes() {
		assertThat(value.getValue(),
				is(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(initialMetric)));
	}

	@Override
	protected final D createValue() {
		initialMetric = Math.PI;
		return createValue(initialMetric);
	}

	protected abstract D createValue(double initialMetric);
}
