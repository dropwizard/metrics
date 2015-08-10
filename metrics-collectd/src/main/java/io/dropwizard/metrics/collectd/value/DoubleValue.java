package io.dropwizard.metrics.collectd.value;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.dropwizard.metrics.collectd.Value;

public class DoubleValue extends Value {
	public DoubleValue(final DataType dataType, final double value) {
		super(dataType, ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(value));
	}
}
