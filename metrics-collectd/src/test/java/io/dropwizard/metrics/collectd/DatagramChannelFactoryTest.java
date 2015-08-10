package io.dropwizard.metrics.collectd;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.channels.DatagramChannel;

import org.junit.Test;

public class DatagramChannelFactoryTest {

	@Test
	public void shouldOpenANewChannel() throws IOException {
		try (DatagramChannel channel1 = DatagramChannelFactory.getDefault().openDatagramChannel()) {
			assertThat(channel1.isOpen(), is(true));
			try (DatagramChannel channel2 = DatagramChannelFactory.getDefault().openDatagramChannel()) {
				assertThat(channel2.isOpen(), is(true));
				assertThat(channel1, is(not(channel2)));
			}
		}
	}
}
