package io.dropwizard.metrics.collectd.value;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

public abstract class LongValueTest<L extends LongValue> extends ValueTest<L> {

	private long initialMetric;

	@Test
	public void shouldEncodeTheInitialValueAsBytes() {
		assertThat(value.getValue(),
				is(ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(initialMetric).array()));
	}

	@Override
	protected final L createValue() {
		initialMetric = System.nanoTime();
		return createValue(initialMetric);
	}

	protected abstract L createValue(long initialMetric);
}
