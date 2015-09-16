package io.dropwizard.metrics.atsd;

import io.dropwizard.metrics.MetricName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * A client to a ATSD server via TCP.
 */
public class AtsdTCPSender implements AtsdSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtsdTCPSender.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final InetSocketAddress address;
    private final SocketFactory socketFactory;
    private final Charset charset;

    private Socket socket;
    private Writer writer;
    private int failures;

    /**
     * Creates a new client which connects to the given address using the default
     * {@link SocketFactory}.
     *
     * @param hostname The hostname of the Carbon server
     * @param port     The port of the Carbon server
     */
    public AtsdTCPSender(String hostname, int port) {
        this(hostname, port, SocketFactory.getDefault());
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param hostname      The hostname of the Carbon server
     * @param port          The port of the Carbon server
     * @param socketFactory the socket factory
     */
    public AtsdTCPSender(String hostname, int port, SocketFactory socketFactory) {
        this(hostname, port, socketFactory, UTF_8);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param hostname      The hostname of the Carbon server
     * @param port          The port of the Carbon server
     * @param socketFactory the socket factory
     * @param charset       the character set used by the server
     */
    public AtsdTCPSender(String hostname, int port, SocketFactory socketFactory, Charset charset) {
        this(new InetSocketAddress(hostname, port), socketFactory, charset);
    }

    /**
     * Creates a new client which connects to the given address using the default
     * {@link SocketFactory}.
     *
     * @param address the address of the Carbon server
     */
    public AtsdTCPSender(InetSocketAddress address) {
        this(address, SocketFactory.getDefault());
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param address       the address of the Carbon server
     * @param socketFactory the socket factory
     */
    public AtsdTCPSender(InetSocketAddress address, SocketFactory socketFactory) {
        this(address, socketFactory, UTF_8);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param address       the address of the Carbon server
     * @param socketFactory the socket factory
     * @param charset       the character set used by the server
     */
    public AtsdTCPSender(InetSocketAddress address, SocketFactory socketFactory, Charset charset) {
        if (address == null) {
            throw new IllegalArgumentException("address is null");
        }
        if (socketFactory == null) {
            throw new IllegalArgumentException("socketFactory is null");
        }
        this.address = address;
        this.socketFactory = socketFactory;
        this.charset = charset;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            LOGGER.warn("Already connected");
            throw new IllegalStateException("Already connected");
        }
        java.net.InetSocketAddress address = this.address;
        if (address.getAddress() == null) {
            throw new java.net.UnknownHostException(address.getHostName());
        }

        socket = socketFactory.createSocket(address.getAddress(), address.getPort());
        LOGGER.debug("socket created");
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset));
        LOGGER.debug("buffer to write is ready");
    }

    @Override
    public boolean isConnected() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed();
    }

    @Override
    public void send(String entity, MetricName metric, String value, long timestamp)
            throws IOException {
        try {
            String msg = Utils.composeMessage(entity, metric, value, timestamp);
            writer.write(msg);
            LOGGER.debug("series sent: {}", msg);
            failures = 0;
        } catch (IOException e) {
            LOGGER.error("fail to send series by TCP", e);
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
        if (writer != null) {
            writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ex) {
            LOGGER.warn("can not close writer", ex);
            if (socket != null) {
                socket.close();
            }
        } finally {
            socket = null;
            writer = null;
        }
    }
}