package com.codahale.metrics.graphite;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A client to a Carbon server using unconnected UDP
 */
public class GraphiteUDP implements GraphiteSender {

    private final String hostname;
    private final int port;
    private InetSocketAddress address;

    private DatagramChannel datagramChannel = null;
    private int failures;

    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param hostname The hostname of the Carbon server
     * @param port The port of the Carbon server
     */
    public GraphiteUDP(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.address = null;
    }

    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param address the address of the Carbon server
     */
    public GraphiteUDP(InetSocketAddress address) {
        this.hostname = null;
        this.port = -1;
        this.address = address;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }

        // Resolve hostname
        if (hostname != null) {
            address = new InetSocketAddress(hostname, port);
        }

        datagramChannel = DatagramChannel.open();
    }

    @Override
    public boolean isConnected() {
    		return datagramChannel != null && !datagramChannel.socket().isClosed();
    }

    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        try {
            String str = sanitize(name) + ' ' + sanitize(value) + ' ' + Long.toString(timestamp) + '\n';
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
    public void flush() throws IOException {
    	  // Nothing to do
    }

    @Override
    public void close() throws IOException {
        if (datagramChannel != null) {
            try {
                datagramChannel.close();
            } finally {
                datagramChannel = null;
            }
        }
    }

    protected String sanitize(String s) {
        return GraphiteSanitize.sanitize(s);
    }

    DatagramChannel getDatagramChannel() {
        return datagramChannel;
    }

    void setDatagramChannel(DatagramChannel datagramChannel) {
        this.datagramChannel = datagramChannel;
    }

    InetSocketAddress getAddress() {
        return address;
    }

    void setAddress(InetSocketAddress address) {
        this.address = address;
    }
}
