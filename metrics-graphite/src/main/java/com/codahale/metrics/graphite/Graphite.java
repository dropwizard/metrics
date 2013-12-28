package com.codahale.metrics.graphite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * A client to a Carbon server.  Absolutely not thread safe.
 */
public class Graphite implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Graphite.class);
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    // this may be optimistic about Carbon/Graphite
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final InetSocketAddress address;
    private final SocketFactory socketFactory;
    private final Charset charset;

    private volatile Socket socket;
    Writer writer;
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
        this.address = address;
        this.socketFactory = socketFactory;
        this.charset = charset;
    }

    /**
     * Connects to the server.
     *
     * @throws IllegalStateException if the client is already connected
     * @throws IOException           if there is an error connecting
     */
    public void connect() throws IllegalStateException, IOException {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Connecting " + this);
        }
        if (socket != null) {
            throw new IllegalStateException("Already connected");
        }

        final InetSocketAddress resolvedAddress;
        if(address.isUnresolved()){
            resolvedAddress = new InetSocketAddress(address.getHostName(), address.getPort());
        } else {
            resolvedAddress = address;
        }
        this.socket = socketFactory.createSocket(resolvedAddress.getAddress(), resolvedAddress.getPort());
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset));
    }

    /**
     * Sends the given measurement to the server.
     *
     * @param name      the name of the metric
     * @param value     the value of the metric
     * @param timestamp the timestamp of the metric
     * @throws IOException if there was an error sending the metric
     */
    public int send(String name, String value, long timestamp) throws IOException {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("GraphiteSender: " + name + " : " + value);
        }
        final int byteCount;
        try {
            final StringBuilder lineItem = new StringBuilder();
            lineItem.append(sanitize(name))
                    .append(' ')
                    .append(sanitize(value))
                    .append(' ')
                    .append(Long.toString(timestamp))
                    .append('\n');
            final String metricString = lineItem.toString();
            final char[] chars = metricString.toCharArray();
            byteCount = chars.length;
            writer.write(chars);
            writer.flush();
            this.failures = 0;

            return byteCount;
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
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Closing " + this);
        }
        if (socket != null) {
            socket.close();
        }
        this.socket = null;
        this.writer = null;
    }

    protected String sanitize(String s) {
        return WHITESPACE.matcher(s).replaceAll("-");
    }

    @Override
    public String toString() {
        return "Graphite{" +
                "address=" + address +
                ", socketFactory=" + socketFactory +
                ", charset=" + charset +
                ", socket=" + socket +
                ", writer=" + writer +
                ", failures=" + failures +
                '}';
    }
}
