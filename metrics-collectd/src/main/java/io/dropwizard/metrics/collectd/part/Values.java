package io.dropwizard.metrics.collectd.part;

import java.nio.ByteBuffer;
import java.util.List;

import io.dropwizard.metrics.collectd.Part;
import io.dropwizard.metrics.collectd.Value;

public class Values extends Part {
	/**
	 * <pre>
	 * Type 0x0006
	 * Length (16 bit field)
	 * Number of values (16 bit field)
	 * Values, for each one:
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
	private final List<? extends Value> values;

	public Values(final List<? extends Value> values) {
		super(PartType.VALUES);
		this.values = values;
	}

	@Override
	protected final short getValueLength() {
		// Num values + (values.length * (data type +
		// value))
		return (short) (2 + values.size() * 9);
	}

	@Override
	protected final void appendValueTo(final ByteBuffer buffer) {
		buffer.putShort((short) values.size());
		for (final Value value : values) {
			buffer.put(value.getDataType().getCode());
		}
		for (final Value value : values) {
			buffer.put(value.getValue().array());
		}
	}
}
