package io.dropwizard.metrics.collectd.part;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Before;
import org.junit.Test;

public abstract class NumericPartTest<N extends NumericPart> extends PartTest<N> {

	private static final short EXPECTED_LENGTH = 12;
	private long initialMetric;

	private ByteBuffer buf;

	@Before
	public void createBuffer() {
		buf = ByteBuffer.allocate(EXPECTED_LENGTH);
	}

	@Test
	public void shouldHaveAFixedLength() {
		assertThat(part.getLength(), is(EXPECTED_LENGTH));
	}

	@Test
	public void shouldAppendAValueOfTheRightLength() {
		part.appendTo(buf);

		assertThat(buf.position(), is((int) EXPECTED_LENGTH));
	}

	@Test
	public void shouldAppendAHeader() {
		part.appendTo(buf);

		final byte[] actual = buf.array();
		final short code = part.getType().getPartTypeCode();
		assertThat(actual[0], is((byte) ((code & 0xFF00) >>> 8)));
		assertThat(actual[1], is((byte) (code & 0x00FF)));
		assertThat(actual[2], is((byte) ((EXPECTED_LENGTH & 0xFF00) >>> 8)));
		assertThat(actual[3], is((byte) (EXPECTED_LENGTH & 0x00FF)));
	}

	@Test
	public void shouldAppendTheValue() {
		final byte[] expected = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(initialMetric).array();

		part.appendTo(buf);

		final byte[] actual = new byte[8];
		System.arraycopy(buf.array(), 4, actual, 0, actual.length);

		assertThat(actual, is(expected));
	}

	@Override
	protected final N createPart() {
		initialMetric = System.nanoTime();
		return createPart(initialMetric);
	}

	protected abstract N createPart(long initialMetric);
}
