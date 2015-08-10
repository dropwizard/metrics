package io.dropwizard.metrics.collectd.part;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Test;

import io.dropwizard.metrics.collectd.Value;
import io.dropwizard.metrics.collectd.value.Absolute;
import io.dropwizard.metrics.collectd.value.Counter;
import io.dropwizard.metrics.collectd.value.Derive;
import io.dropwizard.metrics.collectd.value.Gauge;

public class ValuesTest extends PartTest<Values> {

	private List<Value> values;
	private Absolute absolute;
	private Counter counter;
	private Derive derive;
	private Gauge gauge;

	@Override
	protected Values createPart() {
		absolute = new Absolute(System.nanoTime());
		counter = new Counter(System.nanoTime());
		derive = new Derive(System.nanoTime());
		gauge = new Gauge(Math.PI);
		values = asList(absolute, counter, derive, gauge);
		return new Values(values);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.VALUES;
	}

	private int getExpectedValuesLength() {
		return 2 + values.size() * 9;
	}

	private short getExpectedLength() {
		return (short) (4 + getExpectedValuesLength());
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
		assertThat(actual[4], is((byte) ((values.size() & 0xFF00) >>> 8)));
		assertThat(actual[5], is((byte) (values.size() & 0x00FF)));
	}

	@Test
	public void shouldAppendTypesFirst() {
		final ByteBuffer buf = ByteBuffer.allocate(getExpectedLength());

		part.appendTo(buf);

		final byte[] actual = buf.array();
		for (int i = 0; i < values.size(); i++) {
			assertThat(actual[i + 6], is(values.get(i).getDataType().getCode()));
		}
	}

	@Test
	public void shouldAppendValuesAfterTypes() {
		final ByteBuffer buf = ByteBuffer.allocate(getExpectedLength());

		part.appendTo(buf);

		final byte[] actual = buf.array();
		for (int i = 0; i < values.size(); i++) {
			final byte[] expected = values.get(i).getValue();
			for (int j = 0, k = 6 + values.size() + i * 8; j < expected.length; j++, k++) {
				assertThat(actual[k], is(expected[j]));
			}
		}
	}
}
