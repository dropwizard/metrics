package io.dropwizard.metrics.collectd;

import java.nio.ByteBuffer;

import io.dropwizard.metrics.collectd.part.PartType;

public abstract class Part {

	private final PartType partType;

	protected Part(final PartType partType) {
		this.partType = partType;
	}

	public PartType getType() {
		return partType;
	}

	public final short getLength() {
		return (short) (4 + getValueLength());
	}

	public final void appendTo(final ByteBuffer buffer) {
		buffer.putShort(partType.getPartTypeCode()).putShort(getLength());
		appendValueTo(buffer);
	}

	protected abstract short getValueLength();

	protected abstract void appendValueTo(final ByteBuffer buffer);
}
