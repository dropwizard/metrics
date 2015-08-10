package io.dropwizard.metrics.collectd.value;

import java.nio.ByteBuffer;

import io.dropwizard.metrics.collectd.Value;

public class LongValue extends Value {
	public LongValue(final DataType dataType, final long value) {
		super(dataType, ByteBuffer.allocate(8).putLong(value));
	}
}
