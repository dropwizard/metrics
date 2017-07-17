package com.codahale.metrics.graphite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A client to a Carbon server via TCP.
 */
public class Graphite implements GraphiteSender {
    // this may be optimistic about Carbon/Graphite

    private final String hostname;
    private final int port;
    private final InetSocketAddress address;
    private final SocketFactory socketFactory;
    private final Charset charset;

    private Socket socket;
    private Writer writer;
    private int failures;

    private static final Logger LOGGER = LoggerFactory.getLogger(Graphite.class);

    /**
     * Creates a new client which connects to the given address using the default
     * {@link SocketFactory}.
     *
     * @param hostname The hostname of the Carbon server
     * @param port The port of the Carbon server
     */
    public Graphite(String hostname, int port) {
        this(hostname, port, SocketFactory.getDefault());
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param hostname The hostname of the Carbon server
     * @param port The port of the Carbon server
     * @param socketFactory the socket factory
     */
    public Graphite(String hostname, int port, SocketFactory socketFactory) {
        this(hostname, port, socketFactory, UTF_8);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param hostname The hostname of the Carbon server
     * @param port The port of the Carbon server
     * @param socketFactory the socket factory
     * @param charset       the character set used by the server
     */
    public Graphite(String hostname, int port, SocketFactory socketFactory, Charset charset) {
        this.hostname = hostname;
        this.port = port;
        this.address = null;
        this.socketFactory = socketFactory;
        this.charset = charset;
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
     * @param address       the address of the Carbon server
     * @param socketFactory the socket factory
     */
    public Graphite(InetSocketAddress address, SocketFactory socketFactory) {
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
    public Graphite(InetSocketAddress address, SocketFactory socketFactory, Charset charset) {
        this.hostname = null;
        this.port = -1;
        this.address = address;
        this.socketFactory = socketFactory;
        this.charset = charset;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }
        InetSocketAddress address = this.address;
        if (address == null) {
            address = new InetSocketAddress(hostname, port);
        }
        if (address.getAddress() == null) {
            // retry lookup, just in case the DNS changed
            address = new InetSocketAddress(address.getHostName(),address.getPort());

            if (address.getAddress() == null) {
                throw new UnknownHostException(address.getHostName());
            }
        }

        this.socket = socketFactory.createSocket(address.getAddress(), address.getPort());
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
