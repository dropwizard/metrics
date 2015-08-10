package io.dropwizard.metrics.collectd.part;

import java.nio.ByteBuffer;

import io.dropwizard.metrics.collectd.Part;

public abstract class NumericPart extends Part {

	private final long value;

	public NumericPart(final PartType partType, final long value) {
		super(partType);
		this.value = value;
	}

	@Override
	protected final short getValueLength() {
		return 8;
	}

	@Override
	protected final void appendValueTo(final ByteBuffer buffer) {
		buffer.putLong(value);
	}
}
