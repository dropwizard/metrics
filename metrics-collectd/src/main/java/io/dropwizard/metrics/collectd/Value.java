package io.dropwizard.metrics.collectd;

import java.nio.ByteBuffer;

import io.dropwizard.metrics.collectd.value.DataType;

public abstract class Value {
	/**
	 * <pre>
	 * Data type code (8 bit field)
	 * COUNTER → 0
	 * GAUGE → 1
	 * DERIVE → 2
	 * ABSOLUTE → 3
	 * Value (64 bit field)
	 * COUNTER → network (big endian) unsigned integer
	 * GAUGE → x86 (little endian) double
	 * DERIVE → network (big endian) signed integer
	 * ABSOLUTE → network (big endian) unsigned integer
	 * </pre>
	 */
	private final ByteBuffer value;
	private final DataType dataType;

	protected Value(final DataType dataType, final ByteBuffer value) {
		this.dataType = dataType;
		this.value = value;
	}

	public DataType getDataType() {
		return dataType;
	}

	public ByteBuffer getValue() {
		return value;
	}
}
