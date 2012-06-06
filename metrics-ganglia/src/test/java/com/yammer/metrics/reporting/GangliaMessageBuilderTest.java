package com.yammer.metrics.reporting;

import org.junit.Test;

import java.net.SocketException;

import static org.junit.Assert.assertEquals;

public class GangliaMessageBuilderTest {
    @Test
    public void providesCorrectHostAndPort() throws SocketException {
        final String hostName = "hostName";
        final int port = 12345;

        final GangliaMessageBuilder builder = new GangliaMessageBuilder(hostName, port);

        assertEquals(hostName, builder.getHostName());
        assertEquals(port, builder.getPort());
    }
}
