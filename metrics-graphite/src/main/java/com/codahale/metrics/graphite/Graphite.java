package com.codahale.metrics.graphite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * A client to a Carbon server via TCP.
 */
public class Graphite implements GraphiteSender {
    // this may be optimistic about Carbon/Graphite

    private static final Integer DEFAULT_CONNECTION_TIMEOUT_MS = 500;
    private static final Integer DEFAULT_SOCKET_TIMEOUT_MS = 5000;

    private final String hostname;
    private final int port;
    private final InetSocketAddress address;
    private final SocketFactory socketFactory;
    private final Charset charset;
    private final int connectionTimeoutMs;
    private final int socketTimeoutMs;

    private Socket socket;
    private Writer writer;
    private int failures;

    private static final Logger LOGGER = LoggerFactory.getLogger(Graphite.class);

    /**
     * Creates a new client which connects to the given address using the default
     * {@link SocketFactory}.
     *
     * @param hostname The hostname of the Carbon server
     * @param port     The port of the Carbon server
     */
    public Graphite(String hostname, int port) {
        this(hostname, port, SocketFactory.getDefault());
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param hostname      The hostname of the Carbon server
     * @param port          The port of the Carbon server
     * @param socketFactory The socket factory
     */
    public Graphite(String hostname, int port, SocketFactory socketFactory) {
        this(hostname, port, socketFactory, UTF_8);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param hostname      The hostname of the Carbon server
     * @param port          The port of the Carbon server
     * @param socketFactory The socket factory
     * @param charset       The character set used by the server
     */
    public Graphite(String hostname, int port, SocketFactory socketFactory, Charset charset) {
        this(hostname, port, socketFactory, charset, DEFAULT_CONNECTION_TIMEOUT_MS, DEFAULT_SOCKET_TIMEOUT_MS);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set and timeout.
     *
     * @param hostname            The hostname of the Carbon server
     * @param port                The port of the Carbon server
     * @param socketFactory       The socket factory
     * @param charset             The character set used by the server
     * @param connectionTimeoutMs Timeout in milliseconds for connecting to the server
     * @param socketTimeoutMs     Timeout in milliseconds for writing to the server
     */
    public Graphite(
        String hostname,
        int port,
        SocketFactory socketFactory,
        Charset charset,
        int connectionTimeoutMs,
        int socketTimeoutMs) {
        if (hostname == null || hostname.isEmpty()) {
            throw new IllegalArgumentException("hostname must not be null or empty");
        }

        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be a valid IP port (0-65535)");
        }

        this.hostname = hostname;
        this.port = port;
        this.address = null;
        this.socketFactory = requireNonNull(socketFactory, "socketFactory must not be null");
        this.charset = requireNonNull(charset, "charset must not be null");
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.socketTimeoutMs = socketTimeoutMs;
    }

    /**
     * Creates a new client which connects to the given address using the default
     * {@link SocketFactory}.
     *
     * @param address the address of the Carbon server
     */
    public Graphite(InetSocketAddress address) {
        this(address, SocketFactory.getDefault());
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param address       The address of the Carbon server
     * @param socketFactory The socket factory
     */
    public Graphite(InetSocketAddress address, SocketFactory socketFactory) {
        this(address, socketFactory, UTF_8);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param address       The address of the Carbon server
     * @param socketFactory The socket factory
     * @param charset       The character set used by the server
     */
    public Graphite(InetSocketAddress address, SocketFactory socketFactory, Charset charset) {
        this(address, socketFactory, charset, DEFAULT_CONNECTION_TIMEOUT_MS, DEFAULT_SOCKET_TIMEOUT_MS);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param address             The address of the Carbon server
     * @param socketFactory       The socket factory
     * @param charset             The character set used by the server
     * @param connectionTimeoutMs Timeout in milliseconds for connecting to the server
     * @param socketTimeoutMs     Timeout in milliseconds for writing to the server
     */
    public Graphite(
        InetSocketAddress address,
        SocketFactory socketFactory,
        Charset charset,
        int connectionTimeoutMs,
        int socketTimeoutMs) {
        this.hostname = null;
        this.port = -1;
        this.address = requireNonNull(address, "address must not be null");
        this.socketFactory = requireNonNull(socketFactory, "socketFactory must not be null");
        this.charset = requireNonNull(charset, "charset must not be null");
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.socketTimeoutMs = socketTimeoutMs;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }
        InetSocketAddress address = this.address;
        // the previous dns retry logic did not work, as address.getAddress would always return the cached value
        // this version of the simplified logic will always cause a dns request if hostname has been supplied.
        // InetAddress.getByName forces the dns lookup
        // if an InetSocketAddress was supplied at create time that will take precedence.
        if (address == null || address.getHostName() == null && hostname != null) {
            address = new InetSocketAddress(hostname, port);
        }

        if (address.getAddress() == null) {
            throw new UnknownHostException(address.getHostName());
        }

        this.socket = socketFactory.createSocket();
        this.socket.connect(address, connectionTimeoutMs);
        this.socket.setSoTimeout(socketTimeoutMs);
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset));
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        try {
            writer.write(sanitize(name));
            writer.write(' ');
            writer.write(sanitize(value));
            writer.write(' ');
            writer.write(Long.toString(timestamp));
            writer.write('\n');
            this.failures = 0;
        } catch (IOException e) {
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
            LOGGER.debug("Error closing writer", ex);
        } finally {
            this.writer = null;
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            LOGGER.debug("Error closing socket", ex);
        } finally {
            this.socket = null;
        }
    }

    protected String sanitize(String s) {
        return GraphiteSanitize.sanitize(s);
    }
}
