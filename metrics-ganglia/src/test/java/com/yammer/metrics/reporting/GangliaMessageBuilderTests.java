package com.yammer.metrics.reporting;

import java.net.SocketException;

import org.junit.Test;
import static org.junit.Assert.*;

public class GangliaMessageBuilderTests
{
    @Test
    public void providesCorrectHostAndPort() throws SocketException
    {
        String hostName = "hostName";
        int port = 12345;
        
        GangliaMessageBuilder builder = new GangliaMessageBuilder(hostName, port);
        
        assertEquals(hostName, builder.getHostName());
        assertEquals(port, builder.getPort());
    }
}
