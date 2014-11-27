package com.codahale.metrics.graphite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by p.willoughby on 27/11/14.
 */
public class PickledGraphiteUDP implements GraphiteSender {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final Logger LOGGER = LoggerFactory.getLogger(PickledGraphite.class);
    private final static int DEFAULT_BATCH_SIZE = 100;

    private int batchSize;
    // graphite expects a python-pickled list of nested tuples.
    private List<GraphitePickler.MetricTuple> metrics = new LinkedList<GraphitePickler.MetricTuple>();

    private DatagramChannel datagramChannel = null;
    private int failures;
    private final InetSocketAddress address;
    private final Charset charset;

    /**
     * Creates a new client which connects to the given address using the default {@link SocketFactory}. This defaults
     * to a batchSize of 100
     *
     * @param address
     *            the address of the Carbon server
     */
    public PickledGraphiteUDP(InetSocketAddress address) {
        this(address, DEFAULT_BATCH_SIZE);
    }

    /**
     * Creates a new client which connects to the given address using the default {@link SocketFactory}.
     *
     * @param address
     *            the address of the Carbon server
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphiteUDP(InetSocketAddress address, int batchSize) {
        this(address, UTF_8, batchSize);
    }

    /**
     * Creates a new client which connects to the given address using the given character set.
     *
     * @param address
     *            the address of the Carbon server
     * @param charset
     *            the character set used by the server
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphiteUDP(InetSocketAddress address, Charset charset, int batchSize) {
        this.address = address;
        this.charset = charset;
        this.batchSize = batchSize;
    }

    /**
     * Creates a new client which connects to the given address using the default {@link SocketFactory}. This defaults
     * to a batchSize of 100
     *
     * @param hostname
     *            the hostname of the Carbon server
     * @param port
     *            the port of the Carbon server
     */
    public PickledGraphiteUDP(String hostname, int port) {
        this(hostname, port, DEFAULT_BATCH_SIZE);
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param hostname
     *            the hostname of the Carbon server
     * @param port
     *            the port of the Carbon server
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphiteUDP(String hostname, int port, int batchSize) {
        this(hostname, port, UTF_8, batchSize);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given character set.
     *
     * @param hostname
     *            the hostname of the Carbon server
     * @param port
     *            the port of the Carbon server
     * @param charset
     *            the character set used by the server
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphiteUDP(String hostname, int port, Charset charset, int batchSize) {
        this(new InetSocketAddress(hostname, port), charset, batchSize);
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }
        InetSocketAddress address = this.address;
        if (address.getAddress() == null) {
            throw new UnknownHostException(address.getHostName());
        }

        datagramChannel = DatagramChannel.open();
        datagramChannel.connect(address);
    }

    @Override
    public boolean isConnected() {
        return datagramChannel != null && !datagramChannel.socket().isClosed();
    }

    /**
     * Convert the metric to a python tuple of the form:
     * <p/>
     * (timestamp, (name, value))
     * <p/>
     * And add it to the list of metrics. If we reach the batch size, write them out.
     *
     * @param name
     *            the name of the metric
     * @param value
     *            the value of the metric
     * @param timestamp
     *            the timestamp of the metric
     * @throws IOException
     *             if there was an error sending the metric
     */
    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        metrics.add(new GraphitePickler.MetricTuple(name, timestamp, value));

        if (metrics.size() >= batchSize) {
            writeMetrics();
        }
    }

    @Override
    public void flush() throws IOException {
        // No such concept in UDP
    }

    @Override
    public void close() throws IOException {
        datagramChannel.socket().close();
        datagramChannel = null;
    }

    @Override
    public int getFailures() {
        return failures;
    }

    /**
     * 1. Run the pickler script to package all the pending metrics into a single message
     * 2. Send the message to graphite
     * 3. Clear out the list of metrics
     */
    private void writeMetrics() throws IOException {
        if (!isConnected()) {
            connect();
        }
        if (metrics.size() > 0) {
            try {
                byte[] payload = GraphitePickler.pickleMetrics(metrics,charset);
                ByteBuffer byteBuffer = ByteBuffer.allocate(4 + payload.length).putInt(payload.length).put(payload);

                datagramChannel.send(byteBuffer, address);
                this.failures = 0;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Wrote {} metrics", metrics.size());
                }
            } catch (IOException e) {
                failures++;
                throw e;
            } finally {
                // if there was an error, we might miss some data. for now, drop those on the floor and
                // try to keep going.
                metrics.clear();
            }
        }
    }
}
