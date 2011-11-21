package com.yammer.metrics.reporting;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 *Builder for creating Ganglia messages. Note, this builder is not thread safe (the message buffer is reused between messages)
 */
public class GangliaMessageBuilder
{
    private final InetSocketAddress inetSocketAddress;
    private final byte[] buffer = new byte[1500];
    private final DatagramSocket datagramSocket;
    
    public GangliaMessageBuilder(String hostName, int port) throws SocketException
    {
        this.inetSocketAddress = new InetSocketAddress(hostName, port);
        this.datagramSocket = new DatagramSocket();
    }
    
    /**
     * Create a new Ganglia message
     * @return 
     */
    public GangliaMessage newMessage()
    {
        return new GangliaMessage(this.inetSocketAddress, this.buffer, this.datagramSocket);
    }
    
    public String getHostName()
    {
        return this.inetSocketAddress.getHostName();
    }
    
    public int getPort()
    {
        return this.inetSocketAddress.getPort();
    }
}
