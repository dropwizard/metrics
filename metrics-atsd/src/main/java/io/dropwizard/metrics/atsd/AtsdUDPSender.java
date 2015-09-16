package io.dropwizard.metrics.atsd;

import io.dropwizard.metrics.MetricName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;

/**
 * A client to a ATSD server via UDP.
 */
public class AtsdUDPSender implements AtsdSender {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final Logger LOGGER = LoggerFactory.getLogger(AtsdUDPSender.class);

    private InetSocketAddress address;
    private DatagramChannel datagramChannel = null;
    private int failures;

    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param hostname The hostname of the Carbon server
     * @param port     The port of the Carbon server
     */
    public AtsdUDPSender(String hostname, int port) {
        this(new InetSocketAddress(hostname, port));
    }

    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param address the address of the Carbon server
     */
    public AtsdUDPSender(InetSocketAddress address) {
        if(address == null) {
            throw new IllegalArgumentException("address is null");
        }
        this.address = address;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            LOGGER.warn("Already connected");
            throw new IllegalStateException("Already connected");
        }

        if (datagramChannel != null) {
            datagramChannel.close();
        }

        datagramChannel = DatagramChannel.open();
        LOGGER.debug("datagram channel is open");
    }

    @Override
    public boolean isConnected() {
        return datagramChannel != null && !datagramChannel.socket().isClosed();
    }

    @Override
    public void send(String entity, MetricName metric, String value, long timestamp) throws IOException {
        try {
            if (!isConnected()) {
                connect();
            }
            String msg = Utils.composeMessage(entity, metric, value, timestamp);
            ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes(UTF_8));
            datagramChannel.send(byteBuffer, address);
            LOGGER.debug("series sent: {}", msg);
            failures = 0;
        } catch (IOException e) {
            LOGGER.error("fail to send series by UDP", e);
            failures++;
            throw e;
        }
    }

    @Override
    public int getFailures() {
        return failures;
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
}
