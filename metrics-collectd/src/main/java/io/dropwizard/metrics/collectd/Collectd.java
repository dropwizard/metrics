package io.dropwizard.metrics.collectd;

import io.dropwizard.metrics.MetricName;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client to a Collectd server
 */
public class Collectd {

	private static final Logger LOGGER = LoggerFactory.getLogger(Collectd.class);
	private final String collectdHostname;
	private final int collectdPort;

	private String hostname;
	private InetSocketAddress address;

	private DatagramChannel datagramChannel = null;

	/**
	 * Creates a new client which sends data to given address using UDP
	 *
	 * @param hostname
	 *            The hostname of the l server
	 * @param port
	 *            The port of the Collectd server
	 */
	public Collectd(String hostname, int port) {
		this.collectdHostname = hostname;
		this.collectdPort = port;
		this.address = null;
	}

	/**
	 * Creates a new client which sends data to given address using UDP
	 *
	 * @param address
	 *            the address of the Collectd server
	 */
	public Collectd(InetSocketAddress address) {
		this.collectdHostname = null;
		this.collectdPort = -1;
		this.address = address;
	}

	public void connect() throws IllegalStateException, IOException {
		// Only open the channel the first time...
		if (isConnected()) {
			throw new IllegalStateException("Already connected");
		}

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

		if (datagramChannel != null) {
			datagramChannel.close();
		}

		// Resolve hostname
		if (collectdHostname != null) {
			address = new InetSocketAddress(collectdHostname, collectdPort);
		}

		datagramChannel = DatagramChannel.open();
	}

	public boolean isConnected() {
		return datagramChannel != null && !datagramChannel.socket().isClosed();
	}

	public void send(MetricName name, long value, long timestamp, DataType type, long period) {
		int commaIndex = name.getKey().lastIndexOf(MetricName.SEPARATOR);
		if (commaIndex == -1) {
			send(name.getKey(), "0", value, timestamp, type, period);
		} else {
			send(name.getKey().substring(0, commaIndex), name.getKey().substring(commaIndex + 1), value, timestamp, type, period);
		}
	}

	public void send(MetricName name, double value, long timestamp, DataType type, long period) {
		int commaIndex = name.getKey().lastIndexOf(MetricName.SEPARATOR);
		if (commaIndex == -1) {
			send(name.getKey(), "0", value, timestamp, type, period);
		} else {
			send(name.getKey().substring(0, commaIndex), name.getKey().substring(commaIndex + 1), value, timestamp, type, period);
		}
	}

	public void send(MetricName name, Number value, long timestamp, DataType type, long period) {
		if ((value instanceof Double) || (value instanceof Float)) {
			send(name, value.doubleValue(), timestamp, type, period);
		} else {
			send(name, value.longValue(), timestamp, type, period);
		}
	}

	public void send(MetricName name, String typeInstance, long value, long timestamp, DataType type, long period) {
		send(name.getKey(), typeInstance, value, timestamp, type, period);
	}

	public void send(MetricName name, String typeInstance, double value, long timestamp, DataType type, long period) {
		send(name.getKey(), typeInstance, value, timestamp, type, period);
	}

	private void send(String name, String typeInstance, long value, long timestamp, DataType dataType, long period) {
		send(name, typeInstance, value, true, timestamp, dataType, period);
	}

	private void send(String name, String typeInstance, double value, long timestamp, DataType dataType, long period) {
		send(name, typeInstance, Double.doubleToRawLongBits(value), false, timestamp, dataType, period);
	}

	private void send(String name, String typeInstance, long value, boolean bigEndian, long timestamp, DataType dataType, long period) {
		final String pluginInstance;
		final String plugin;
		int typeIndex = name.lastIndexOf(MetricName.SEPARATOR);
		if (typeIndex == -1) {
			pluginInstance = "0";
			plugin = name;
		} else {
			pluginInstance = name.substring(typeIndex + 1);
			plugin = name.substring(0, typeIndex);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(Network.BUFFER_SIZE);
		DataOutputStream buffer = new DataOutputStream(baos);
		try {
			writeString(buffer, Network.TYPE_HOST, hostname);
			writeNumber(buffer, Network.TYPE_TIME, timestamp / 1000);
			writeString(buffer, Network.TYPE_PLUGIN, plugin);
			writeString(buffer, Network.TYPE_PLUGIN_INSTANCE, pluginInstance);
			writeString(buffer, Network.TYPE_TYPE, dataType.name().toLowerCase());
			writeString(buffer, Network.TYPE_TYPE_INSTANCE, typeInstance);
			writeNumber(buffer, Network.TYPE_INTERVAL, period);

			int len = Network.HEADER_LEN + Network.UINT16_LEN + Network.UINT8_LEN + Network.UINT64_LEN;
			writeHeader(buffer, Network.TYPE_VALUES, len);
			buffer.writeShort((short) 1);
			buffer.write(dataType.getCode());
		} catch (IOException e) {
			LOGGER.error("unable to write bytearray", e);
			return;
		}
		try {
			if (bigEndian) {
				buffer.writeLong(value);
			} else {
				// copy-paste from java.nio.Bits.putLong:
				// do not create intermediate ByteBuffer => less GC and memory
				// footprint
				buffer.write(long7(value));
				buffer.write(long6(value));
				buffer.write(long5(value));
				buffer.write(long4(value));
				buffer.write(long3(value));
				buffer.write(long2(value));
				buffer.write(long1(value));
				buffer.write(long0(value));
			}
			buffer.close();
		} catch (IOException e) {
			LOGGER.error("unable to write bytearray", e);
		}
		if (buffer.size() > Network.BUFFER_SIZE) {
			LOGGER.warn("discard metric: {} size exceed: {}", name, buffer.size());
			return;
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(baos.toByteArray());
		try {
			datagramChannel.send(byteBuffer, address);
		} catch (IOException e) {
			LOGGER.error("unable to write", e);
		}
	}

	private static byte long7(long x) {
		return (byte) (x >> 56);
	}

	private static byte long6(long x) {
		return (byte) (x >> 48);
	}

	private static byte long5(long x) {
		return (byte) (x >> 40);
	}

	private static byte long4(long x) {
		return (byte) (x >> 32);
	}

	private static byte long3(long x) {
		return (byte) (x >> 24);
	}

	private static byte long2(long x) {
		return (byte) (x >> 16);
	}

	private static byte long1(long x) {
		return (byte) (x >> 8);
	}

	private static byte long0(long x) {
		return (byte) (x);
	}

	private static void writeString(DataOutputStream buffer, int type, String val) throws IOException {
		if (val == null || val.length() == 0) {
			return;
		}
		int len = Network.HEADER_LEN + val.length() + 1;
		writeHeader(buffer, type, len);
		buffer.write(val.getBytes(StandardCharsets.US_ASCII));
		buffer.write((byte) '\0');
	}

	private static void writeNumber(DataOutputStream buffer, int type, long val) throws IOException {
		int len = Network.HEADER_LEN + Network.UINT64_LEN;
		writeHeader(buffer, type, len);
		buffer.writeLong(val);
	}

	private static void writeHeader(DataOutputStream buffer, int type, int len) throws IOException {
		buffer.writeShort((short) type);
		buffer.writeShort((short) len);
	}

}
