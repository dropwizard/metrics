package com.yammer.metrics.graphite;

import javax.net.SocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

public class Graphite implements Closeable {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    // this may be optimistic about Carbon/Graphite
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final InetSocketAddress address;
    private final SocketFactory socketFactory;

    private Socket socket;
    private Writer writer;
    private int failures;

    public Graphite(InetSocketAddress address, SocketFactory socketFactory) {
        this.address = address;
        this.socketFactory = socketFactory;
    }

    public void connect() throws IOException {
        if (socket != null) {
            throw new IllegalStateException("Already connected");
        }

        this.socket = socketFactory.createSocket(address.getAddress(), address.getPort());
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8));
    }

    public void write(String name, String value, long timestamp) throws IOException {
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
