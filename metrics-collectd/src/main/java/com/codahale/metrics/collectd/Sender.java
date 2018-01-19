package com.codahale.metrics.collectd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Sender {

    private final String host;
    private final int port;

    private InetSocketAddress address;
    private DatagramChannel channel;

    public Sender(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }
        if (host != null) {
            address = new InetSocketAddress(host, port);
        }
        channel = DatagramChannel.open();
    }

    public boolean isConnected() {
        return channel != null && !channel.socket().isClosed();
    }

    public void send(ByteBuffer buffer) throws IOException {
        channel.send(buffer, address);
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

}
