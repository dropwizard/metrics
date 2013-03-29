package com.yammer.metrics.graphite;

import javax.net.SocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * A client to a Carbon server.
 */
public class Graphite implements Closeable {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    // this may be optimistic about Carbon/Graphite
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final InetSocketAddress address;
    private final SocketFactory socketFactory;

    private Socket socket;
    private Writer writer;
    private int failures;

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
     * @param address the address of the Carbon server
     * @param socketFactory the socket factory
     */
    public Graphite(InetSocketAddress address, SocketFactory socketFactory) {
        this.address = address;
        this.socketFactory = socketFactory;
    }

    /**
     * Connects to the server.
     *
     * @throws IllegalStateException if the client is already connected
     * @throws IOException if there is an error connecting
     */
    public void connect() throws IllegalStateException, IOException {
        if (socket != null) {
            throw new IllegalStateException("Already connected");
        }

        this.socket = socketFactory.createSocket(address.getAddress(), address.getPort());
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
    }

    /**
     * Sends the given measurement to the server.
     *
     * @param name         the name of the metric
     * @param value        the value of the metric
     * @param timestamp    the timestamp of the metric
     * @throws IOException if there was an error sending the metric
     */
    public void send(String name, String value, long timestamp) throws IOException {
        try {
            writer.write(sanitize(name));
            writer.write(' ');
            writer.write(sanitize(value));
            writer.write(' ');
            writer.write(Long.toString(timestamp));
            writer.write('\n');
            writer.flush();
            this.failures = 0;
        } catch (IOException e) {
            failures++;
            throw e;
        }
    }

    /**
     * Returns the number of failed writes to the server.
     *
     * @return the number of failed writes to the server
     */
    public int getFailures() {
        return failures;
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        this.socket = null;
        this.writer = null;
    }

    protected String sanitize(String s) {
        return WHITESPACE.matcher(s).replaceAll("-");
    }
}
