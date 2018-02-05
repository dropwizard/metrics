package io.dropwizard.metrics5.influxdb;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class InfluxDbUdpTest {

    private final String host = "example.com";
    private final int port = 1234;

    private InfluxDbUdpSender influxdbUdp;
    private final DatagramChannel datagramChannel = Mockito.mock(DatagramChannel.class);
    private final List<byte[]> sent = new ArrayList<>();

    @Before
    public void setUp() throws IOException {
        sent.clear();
        doAnswer(invocation -> {
            sent.add(toBytes(invocation.getArgument(0)));
            return 0;
        }).when(datagramChannel).send(any(ByteBuffer.class), any(SocketAddress.class));
        influxdbUdp = new InfluxDbUdpSender(host, port);
        influxdbUdp.setDatagramChannel(datagramChannel);
    }

    @Test
    public void writesValue() throws Exception {
        influxdbUdp.send(new StringBuilder("räksmörgås value=123 456000000000\n"));
        influxdbUdp.flush();

        verify(datagramChannel).send(any(), any());

        assertThat(sent).first().isEqualTo("räksmörgås value=123 456000000000\n".getBytes("UTF-8"));
    }

    @Test
    public void batchesValues() throws Exception {
        influxdbUdp.send(new StringBuilder("name1 value=111 456000000000\n"));
        influxdbUdp.send(new StringBuilder("name2 value=222 456000000000\n"));
        influxdbUdp.flush();

        verify(datagramChannel).send(any(), any());

        assertThat(sent).first().isEqualTo(
                "name1 value=111 456000000000\nname2 value=222 456000000000\n".getBytes("UTF-8"));
    }

    @Test
    public void respectsMTU() throws Exception {
        influxdbUdp.setMTU(40);
        influxdbUdp.send(new StringBuilder("name1 value=111 456000000000\n"));
        influxdbUdp.send(new StringBuilder("name2 value=222 456000000000\n"));
        influxdbUdp.flush();

        verify(datagramChannel, times(2)).send(any(), any());

        assertThat(sent).element(0).isEqualTo("name1 value=111 456000000000\n".getBytes("UTF-8"));
        assertThat(sent).element(1).isEqualTo("name2 value=222 456000000000\n".getBytes("UTF-8"));
    }

    private byte[] toBytes(ByteBuffer buf) {
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        return bytes;
    }
}