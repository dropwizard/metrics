package com.codahale.metrics.graphite;

import org.junit.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class GraphiteUDPTest {

    private final String host = "example.com";
    private final int port = 1234;

    private GraphiteUDP graphiteUDP;

    @Test
    public void connects() throws Exception {
        graphiteUDP = new GraphiteUDP(host, port);
        graphiteUDP.connect();

        assertThat(graphiteUDP.getDatagramChannel()).isNotNull();
        assertThat(graphiteUDP.getAddress()).isEqualTo(new InetSocketAddress(host, port));

        graphiteUDP.close();
    }

    @Test
    public void writesValue() throws Exception {
        graphiteUDP = new GraphiteUDP(host, port);
        DatagramChannel mockDatagramChannel = Mockito.mock(DatagramChannel.class);
        graphiteUDP.setDatagramChannel(mockDatagramChannel);
        graphiteUDP.setAddress(new InetSocketAddress(host, port));

        graphiteUDP.send("name woo", "value", 100);
        verify(mockDatagramChannel).send(ByteBuffer.wrap("name-woo value 100\n".getBytes("UTF-8")),
                new InetSocketAddress(host, port));
    }

}