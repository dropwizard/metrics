package io.dropwizard.metrics.collectd.part;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.dropwizard.metrics.collectd.Part;

public abstract class StringPart extends Part {

	private final byte[] value;

	public StringPart(final PartType partType, final String value, final Charset charset) {
		super(partType);
		this.value = (value + '\0').getBytes(charset);
	}

	@Override
	protected short getValueLength() {
		return (short) value.length;
	}

	@Override
	protected final void appendValueTo(final ByteBuffer buffer) {
		buffer.put(value);
	}
}
