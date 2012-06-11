package com.yammer.metrics.reporting;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class DefaultCachingSocketProvider implements SocketProvider {
	
	private String host;
	private int port;
	
	public DefaultCachingSocketProvider(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	private Socket connection;

	public Socket get() throws UnknownHostException, IOException {
		if (connection != null && !connection.isClosed())
			return connection;
		connection = new Socket(host, port);
		return connection;
	}


}
