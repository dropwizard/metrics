package io.dropwizard.metrics5.influxdb;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;

public class InfluxDbUdpSender implements InfluxDbSender {

    private final InetSocketAddress address;
    private int mtu = 1500;

    private DatagramChannel datagramChannel;
    private ByteBuffer byteBuf;
    private CharBuffer charBuf;

    private final CharsetEncoder encoder = Charset.forName("UTF-8")
            .newEncoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);

    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param hostname The hostname of the InfluxDb server
     * @param port     The port of the InfluxDb server
     */
    public InfluxDbUdpSender(String hostname, int port) {
        this(new InetSocketAddress(hostname, port));
    }

    /**
     * Creates a new client which sends data to given address using UDP
     *
     * @param address the address of the InfluxDb server
     */
    public InfluxDbUdpSender(InetSocketAddress address) {
        this.address = Objects.requireNonNull(address);
        charBuf = CharBuffer.allocate(mtu * 2);
        byteBuf = ByteBuffer.allocate(mtu * 2);
    }

    // for testing
    void setMTU(int mtu) {
        this.mtu = mtu;
    }

    // for testing
    void setDatagramChannel(DatagramChannel datagramChannel) {
        this.datagramChannel = datagramChannel;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (datagramChannel == null) {
            datagramChannel = DatagramChannel.open();
        }
        byteBuf.clear();
    }

    @Override
    public boolean isConnected() {
        return datagramChannel != null;
    }

    @Override
    public void disconnect() throws IOException {
        // ignore, keep the datagram channel open
    }

    @Override
    public void close() throws IOException {
        try {
            datagramChannel.close();
        } finally {
            datagramChannel = null;
        }
    }

    @Override
    public void send(StringBuilder str) throws IOException {
        int len = byteBuf.position();
        encode(str);
        int len2 = byteBuf.position();
        if (len2 >= mtu) {
            if (len == 0) {
                // send current buffer (one single measurement exceeds the MTU)
                sendBuffer();
                byteBuf.clear();
            } else {
                // send previous buffer
                byteBuf.position(len);
                sendBuffer();
                byteBuf.limit(len2);
                byteBuf.compact();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (byteBuf.position() > 0) {
            sendBuffer();
            byteBuf.clear();
        }
    }

    private void sendBuffer() throws IOException {
        byteBuf.flip();
        datagramChannel.send(byteBuf, address);
    }

    private void encode(StringBuilder str) {
        // copy chars
        if (charBuf.capacity() < str.length()) {
            charBuf = CharBuffer.allocate(str.length());
        } else {
            charBuf.clear();
        }
        str.getChars(0, str.length(), charBuf.array(), charBuf.arrayOffset());
        charBuf.limit(str.length());

        // encode chars
        encoder.reset();

        for (; ; ) {
            CoderResult result = encoder.encode(charBuf, byteBuf, true);
            if (result.isOverflow()) {
                // grow the buffer
                ByteBuffer byteBuf2 = ByteBuffer.allocate(byteBuf.capacity() * 2);
                byteBuf.flip();
                byteBuf2.put(byteBuf);
                byteBuf = byteBuf2;
            } else { // underflow, i.e. done
                break;
            }
        }
    }


}
