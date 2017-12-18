package com.codahale.metrics.collectd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

/**
 * A client to a Collectd server
 */
public class Collectd {

    private static final Logger LOG = LoggerFactory.getLogger(Collectd.class);

    private String hostname;
    private int port;
    private InetSocketAddress address;

    private DatagramChannel channel = null;

    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param hostname
     *            The hostname of the l server
     * @param port
     *            The port of the Collectd server
     */
    public Collectd(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param address
     *            the address of the Collectd server
     */
    public Collectd(InetSocketAddress address) {
        this.address = address;
    }

    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }
        if (hostname != null) {
            address = new InetSocketAddress(hostname, port);
        }
        channel = DatagramChannel.open();
    }

    public boolean isConnected() {
        return channel != null && !channel.socket().isClosed();
    }

    public void disconnect() throws IOException {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } finally {
            channel = null;
        }
    }

    public void send(Identifier id, long value, long timestamp, DataType dataType, long period) {
        send(id, toByteArray(value), timestamp, dataType, period);
    }

    public void send(Identifier id, double value, long timestamp, DataType dataType, long period) {
        send(id, toByteArray(value), timestamp, dataType, period);
    }

    private void send(Identifier identifier, byte[] value, long timestamp, DataType dataType, long period) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(Network.BUFFER_SIZE);
        DataOutputStream buffer = new DataOutputStream(baos);
        try {
            writeString(buffer, Network.TYPE_HOST, identifier.host);
            writeNumber(buffer, Network.TYPE_TIME, timestamp / 1000);
            writeString(buffer, Network.TYPE_PLUGIN, identifier.plugin);
            writeString(buffer, Network.TYPE_PLUGIN_INSTANCE, identifier.pluginInstance);
            writeString(buffer, Network.TYPE_TYPE, identifier.type);
            writeString(buffer, Network.TYPE_TYPE_INSTANCE, identifier.typeInstance);
            writeNumber(buffer, Network.TYPE_INTERVAL, period);

            int len = Network.HEADER_LEN + Network.UINT16_LEN + Network.UINT8_LEN + Network.UINT64_LEN;
            writeHeader(buffer, Network.TYPE_VALUES, len);
            buffer.writeShort((short) 1);
            buffer.write(dataType.getCode());
        } catch (IOException e) {
            LOG.error("unable to write bytearray", e);
            return;
        }
        try {
            buffer.write(value);
            buffer.close();
        } catch (IOException e) {
            LOG.error("unable to write bytearray", e);
        }
        if (buffer.size() > Network.BUFFER_SIZE) {
            LOG.warn("discard metric: {} size exceed: {}", identifier, buffer.size());
            return;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(baos.toByteArray());
        try {
            channel.send(byteBuffer, address);
        } catch (IOException e) {
            LOG.error("unable to write", e);
        }
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

    private static byte[] toByteArray(long value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).putLong(value);
        return bytes;
    }

    private static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putDouble(value);
        return bytes;
    }

}

