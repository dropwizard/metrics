package com.codahale.metrics.graphite;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * A client to a Carbon server using unconnected UDP
 */
public class GraphiteUDP implements GraphiteSender {

    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final InetSocketAddress address;

    private DatagramChannel datagramChannel = null;
    private int failures;
    
    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param hostname The hostname of the Carbon server
     * @param port The port of the Carbon server
     */
    public GraphiteUDP(String hostname, int port) {
        this(new InetSocketAddress(hostname, port));
    }

    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param address the address of the Carbon server
     */
    public GraphiteUDP(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        // Only open the channel the first time...
        if (datagramChannel == null) {
            datagramChannel = DatagramChannel.open();
        }
    }

    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        // Underlying socket can be closed by ICMP
        if (datagramChannel.socket().isClosed()) {
            datagramChannel.close();
            datagramChannel = DatagramChannel.open();
        }

        try {
            StringBuilder buf = new StringBuilder();
            buf.append(sanitize(name));
            buf.append(' ');
            buf.append(sanitize(value));
            buf.append(' ');
            buf.append(Long.toString(timestamp));
            buf.append('\n');
            String str = buf.toString();
            ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes(UTF_8));
            datagramChannel.send(byteBuffer, address);
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
    public void close() throws IOException {
        // Leave channel & socket open for next metrics
    }

    protected String sanitize(String s) {
        return WHITESPACE.matcher(s).replaceAll("-");
    }

}
