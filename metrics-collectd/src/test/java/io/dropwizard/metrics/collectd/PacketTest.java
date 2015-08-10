package io.dropwizard.metrics.collectd;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.collectd.part.Host;
import io.dropwizard.metrics.collectd.part.Interval;
import io.dropwizard.metrics.collectd.part.NumericPart;
import io.dropwizard.metrics.collectd.part.StringPart;

public class PacketTest {

	private Packet packet;
	private StringPart stringPart;
	private NumericPart numericPart;

	@Before
	public void setUp() {
		stringPart = new Host(UUID.randomUUID().toString(), Charset.forName("UTF-8"));
		numericPart = new Interval(System.nanoTime());
		packet = new Packet(asList(stringPart, numericPart));
	}

	@Test
	public void shouldSumTheLengthsOfItsConstituentParts() {
		assertThat(packet.getLength(), is(stringPart.getLength() + numericPart.getLength()));
	}

	@Test
	public void shouldConcatenateItsPartsTogether() {
		final byte[] expected = new byte[stringPart.getLength() + numericPart.getLength()];
		final ByteBuffer bb = ByteBuffer.wrap(expected);
		stringPart.appendTo(bb);
		numericPart.appendTo(bb);

		assertThat(packet.build().array(), is(expected));
	}

}
