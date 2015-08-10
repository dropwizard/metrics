package io.dropwizard.metrics.collectd;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import io.dropwizard.metrics.collectd.part.Time;

public class CollectdTest {

	@Test
	public void opensADatagramChannel() throws Exception {
		try (final Collectd collectd = new Collectd()) {
			collectd.connect();

			assertThat(collectd.isConnected(), is(true));
		}
	}

	@Test
	public void disconnectsFromCollectd() throws Exception {
		try (final Collectd collectd = new Collectd()) {
			collectd.connect();

			collectd.close();

			assertThat(collectd.isConnected(), is(false));
		}
	}

	@Test(expected = IllegalStateException.class)
	public void doesNotAllowDoubleConnections() throws Exception {
		try (final Collectd collectd = new Collectd()) {
			collectd.connect();
			collectd.connect();
		}
	}

	@Test(timeout = 500)
	public void writesValuesToCollectd() throws Exception {
		final CountDownLatch bindingLatch = new CountDownLatch(1);
		final CountDownLatch receivedLatch = new CountDownLatch(1);
		final AtomicReference<InetSocketAddress> addr = new AtomicReference<>();
		final Packet packet = new Packet(asList(new Time((int) (System.currentTimeMillis() / 1000))));
		final ByteBuffer buf = ByteBuffer.allocate(packet.getLength());
		buf.clear();
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				DatagramChannel channel;
				try {
					channel = DatagramChannel.open();
					channel.socket().bind(null);
					addr.set((InetSocketAddress) channel.socket().getLocalSocketAddress());
					bindingLatch.countDown();
					channel.receive(buf);
					receivedLatch.countDown();
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		bindingLatch.await();

		final Collectd collectd = new Collectd(addr.get());
		collectd.connect();
		collectd.send(packet);
		collectd.flush();
		collectd.close();
		receivedLatch.await();

		assertThat(buf.array()).isEqualTo(packet.build().array());
	}

	@Test
	public void notifiesIfCollectdIsUnavailable() throws Exception {
		final String unavailableHost = "unknown-host-10el6m7yg56ge7dm.com";
		final InetSocketAddress unavailableAddress = new InetSocketAddress(unavailableHost, 1234);

		try (final Collectd unavailableCollectd = new Collectd(unavailableAddress)) {
			unavailableCollectd.connect();
			failBecauseExceptionWasNotThrown(UnknownHostException.class);
		} catch (final Exception e) {
			assertThat(e.getMessage()).isEqualTo(unavailableHost);
		}
	}
}
