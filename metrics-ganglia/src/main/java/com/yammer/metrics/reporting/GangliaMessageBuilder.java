package com.yammer.metrics.reporting;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class GangliaMessageBuilder
{
    private final InetSocketAddress inetSocketAddress;
    private final byte[] buffer = new byte[1500];
    private final DatagramSocket datagramSocket;
    
    public GangliaMessageBuilder(String hostName, int port, DatagramSocket datagramSocket)
    {
        this.inetSocketAddress = new InetSocketAddress(hostName, port);
        this.datagramSocket = datagramSocket;
    }
    
    public GangliaMessage newMessage()
    {
        return new GangliaMessage(this.inetSocketAddress, this.buffer, this.datagramSocket);
    }
}
