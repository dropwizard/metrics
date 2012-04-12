package com.yammer.metrics.reporting;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Encapsulates logic for creating and sending a Ganglia message
 */
class GangliaMessage {

    private final byte[] buffer;
    private int offset = 0;
    private final DatagramSocket datagramSocket;
    private final InetSocketAddress inetSocketAddress;

    GangliaMessage(InetSocketAddress inetSocketAddress, byte[] buffer, DatagramSocket datagramSocket) {
        this.inetSocketAddress = inetSocketAddress;
        this.buffer = buffer;
        this.datagramSocket = datagramSocket;
    }

    /**
     * Creates and sends a new {@link DatagramPacket}
     *
     * @throws IOException if there is an error sending the packet
     */
    public void send() throws IOException {
        this.datagramSocket
                .send(new DatagramPacket(this.buffer, this.offset, this.inetSocketAddress));
    }

    /**
     * Puts an integer into the buffer as 4 bytes, big-endian.
     *
     * @param value the integer to write to the buffer
     * @return {@code this}
     */
    public GangliaMessage addInt(int value) {
        this.buffer[this.offset++] = (byte) ((value >> 24) & 0xff);
        this.buffer[this.offset++] = (byte) ((value >> 16) & 0xff);
        this.buffer[this.offset++] = (byte) ((value >> 8) & 0xff);
        this.buffer[this.offset++] = (byte) (value & 0xff);

        return this;
    }

    /**
     * Puts a string into the buffer by first writing the size of the string as an int, followed by
     * the bytes of the string, padded if necessary to a multiple of 4.
     *
     * @param value the message to write to the buffer
     * @return {@code this}
     */
    public GangliaMessage addString(String value) {
        final byte[] bytes = value.getBytes();
        final int len = bytes.length;
        addInt(len);
        System.arraycopy(bytes, 0, this.buffer, this.offset, len);
        this.offset += len;
        pad();

        return this;
    }

    /**
     * Pads the buffer with zero bytes up to the nearest multiple of 4.
     */
    private void pad() {
        final int newOffset = ((this.offset + 3) / 4) * 4;
        while (this.offset < newOffset) {
            this.buffer[this.offset++] = 0;
        }
    }

    int getOffset() {
        return this.offset;
    }
}
