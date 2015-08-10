package io.dropwizard.metrics.collectd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client to a collectd server using unconnected UDP
 */
public class Collectd implements CollectdSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(Collectd.class);

	public static final String DEFAULT_HOST_NAME = "localhost";
	public static final String NET_DEFAULT_V4_ADDR = "239.192.74.66";
	public static final String NET_DEFAULT_V6_ADDR = "ff18::efc0:4a42";
	public static final int NET_DEFAULT_PORT = 25826;
	public static final int DEFAULT_PACKET_SIZE = 1452;

	private final InetSocketAddress address;
	private final DatagramChannelFactory datagramChannelFactory;
	private final AtomicReference<DatagramChannel> datagramChannel = new AtomicReference<>();
	private final ByteBuffer buf;

	/**
	 * Creates a new client which connects to the given address using the
	 * default {@link DatagramChannelFactory}.
	 *
	 * @param hostname
	 *            The hostname of the Collectd server
	 * @param port
	 *            The port of the Collectd server
	 */
	public Collectd(final String hostname, final int port) {
		this(hostname, port, DatagramChannelFactory.getDefault());
	}

	/**
	 * Creates a new client which connects to the given address and
	 * DatagramChannel factory.
	 *
	 * @param hostname
	 *            The hostname of the Collectd server
	 * @param port
	 *            The port of the Collectd server
	 * @param datagramChannelFactory
	 *            the datagramChannelFactory factory
	 */
	public Collectd(final String hostname, final int port, final DatagramChannelFactory datagramChannelFactory) {
		this(hostname, port, datagramChannelFactory, DEFAULT_PACKET_SIZE);
	}

	/**
	 * Creates a new client which connects to the given address and datagram
	 * channel factory using the given character set.
	 *
	 * @param hostname
	 *            The hostname of the Collectd server
	 * @param port
	 *            The port of the Collectd server
	 * @param datagramChannelFactory
	 *            the datagramChannel factory
	 * @param packetSize
	 *            the max size of the collectd udp packet
	 */
	public Collectd(final String hostname, final int port, final DatagramChannelFactory datagramChannelFactory,
			final int packetSize) {
		this(new InetSocketAddress(hostname, port), datagramChannelFactory, packetSize);
	}

	/**
	 * Creates a new client which connects to the given address using the
	 * default {@link DatagramChannelFactory}.
	 *
	 * @param address
	 *            the address of the Collectd server
	 */
	public Collectd(final InetSocketAddress address) {
		this(address, DatagramChannelFactory.getDefault());
	}

	/**
	 * Creates a new client which connects to the given address and
	 * datagramChannel factory.
	 *
	 * @param address
	 *            the address of the Collectd server
	 * @param datagramChannel
	 *            the datagramChannel factory
	 */
	public Collectd(final InetSocketAddress address, final DatagramChannelFactory datagramChannelFactory) {
		this(address, datagramChannelFactory, DEFAULT_PACKET_SIZE);
	}

	/**
	 * Creates a new client which connects to the given address and
	 * datagramChannel factory using the given character set.
	 *
	 * @param address
	 *            the address of the Collectd server
	 * @param socketFactory
	 *            the datagramChannel factory
	 */
	public Collectd(final InetSocketAddress address, final DatagramChannelFactory datagramChannelFactory,
			final int packetSize) {
		this.address = address;
		this.datagramChannelFactory = datagramChannelFactory;
		buf = ByteBuffer.allocate(packetSize);
		buf.clear();
	}

	/**
	 * Creates a new client which connects to the Collectd server on localhost
	 * with port 25826.
	 */
	public Collectd() {
		this(DEFAULT_HOST_NAME, NET_DEFAULT_PORT);
	}

	@Override
	public void connect() throws IllegalStateException, IOException {
		final InetSocketAddress address = this.address;
		if (address.getAddress() == null) {
			throw new UnknownHostException(address.getHostName());
		}
		final DatagramChannel currentDatagramChannel = datagramChannel.get();
		if (isConnected(currentDatagramChannel)) {
			throw new IllegalStateException("Already connected");
		}
		final DatagramChannel newDatagramChannel = datagramChannelFactory.openDatagramChannel();
		if (datagramChannel.compareAndSet(currentDatagramChannel, newDatagramChannel)) {
			newDatagramChannel.connect(address);
		} else {
			newDatagramChannel.close();
		}
	}

	private boolean isConnected(final DatagramChannel datagramChannel) {
		return datagramChannel != null && datagramChannel.isConnected();
	}

	@Override
	public boolean isConnected() {
		return isConnected(datagramChannel.get());
	}

	@Override
	public void send(final Packet packet) throws IOException {
		final int packetLength = packet.getLength();

		if (packetLength > buf.capacity()) {
			LOGGER.error(String.format("The packet length %d is greater than the capacity %d. Dropping the packet.",
					packetLength, buf.capacity()));
			return;
		}
		if (packetLength <= buf.remaining()) {
			buf.put(packet.build());
		} else {
			buf.flip();
			datagramChannel.get().write(buf);
			buf.compact();
		}
	}

	@Override
	public void flush() throws IOException {
		if (buf.position() > 0) {
			buf.flip();
			datagramChannel.get().write(buf);
		}
		buf.clear();
	}

	@Override
	public void close() throws IOException {
		final DatagramChannel channel = datagramChannel.get();
		if (null != channel) {
			try {
				channel.disconnect();
			} finally {
				channel.close();
			}
		}
	}
}
