package io.dropwizard.metrics.collectd.part;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.UUID;

import org.junit.Test;

public abstract class StringPartTest<S extends StringPart> extends PartTest<S> {

	private static final Charset CHARSET = Charset.forName("UTF-8");
	private String initialMetric;

	private short getExpectedLength() {
		return (short) (4 + getValueLength() + 1);
	}

	private int getValueLength() {
		return initialMetric.getBytes(CHARSET).length;
	}

	@Test
	public void shouldCalculateLength() {
		assertThat(part.getLength(), is(getExpectedLength()));
	}

	@Test
	public void shouldAppendAValueOfTheRightLength() {
		final ByteBuffer buf = ByteBuffer.allocate(getExpectedLength());

		part.appendTo(buf);

		assertThat(buf.remaining(), is(0));
	}

	@Test
	public void shouldAppendAHeader() {
		final ByteBuffer buf = ByteBuffer.allocate(getExpectedLength());

		part.appendTo(buf);

		final byte[] actual = buf.array();
		final short code = part.getType().getPartTypeCode();
		assertThat(actual[0], is((byte) ((code & 0xFF00) >>> 8)));
		assertThat(actual[1], is((byte) (code & 0x00FF)));
		assertThat(actual[2], is((byte) ((getExpectedLength() & 0xFF00) >>> 8)));
		assertThat(actual[3], is((byte) (getExpectedLength() & 0x00FF)));
	}

	@Test
	public void shouldNullTerminateTheString() {
		final ByteBuffer buf = ByteBuffer.allocate(getExpectedLength());

		part.appendTo(buf);

		final byte[] actual = buf.array();
		assertThat(actual[getExpectedLength() - 1], is("\0".getBytes(CHARSET)[0]));
	}

	@Test
	public void shouldAppendTheValue() {
		final ByteBuffer buf = ByteBuffer.allocate(getExpectedLength());
		final byte[] expected = ByteBuffer.allocate(getValueLength()).order(ByteOrder.BIG_ENDIAN)
				.put(initialMetric.getBytes(CHARSET)).array();

		part.appendTo(buf);

		final byte[] actual = new byte[getValueLength()];
		System.arraycopy(buf.array(), 4, actual, 0, actual.length);

		assertThat(actual, is(expected));
	}

	@Override
	protected final S createPart() {
		initialMetric = UUID.randomUUID().toString();
		return createPart(initialMetric, CHARSET);
	}

	protected abstract S createPart(String initialMetric, Charset charset);
}
