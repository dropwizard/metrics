package io.dropwizard.metrics.collectd;

import java.nio.ByteBuffer;
import java.util.List;

public class Packet {

	private final List<? extends Part> parts;

	public Packet(final List<? extends Part> parts) {
		this.parts = parts;
	}

	public List<? extends Part> getParts() {
		return parts;
	}

	public int getLength() {
		int length = 0;
		for (final Part part : parts) {
			length += part.getLength();
		}
		return length;
	}

	public ByteBuffer build() {
		final ByteBuffer byteBuffer = ByteBuffer.allocate(getLength());
		for (final Part part : parts) {
			part.appendTo(byteBuffer);
		}
		byteBuffer.flip();
		return byteBuffer;
	}
}
