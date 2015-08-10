package io.dropwizard.metrics.collectd;

import java.io.IOException;
import java.nio.channels.DatagramChannel;

public abstract class DatagramChannelFactory {
	
	public abstract DatagramChannel openDatagramChannel() throws IOException;
	
	public static DatagramChannelFactory getDefault() {
		return DEFAULT;
	}
	
	private static final DatagramChannelFactory DEFAULT = new DatagramChannelFactory() {
		@Override
		public DatagramChannel openDatagramChannel() throws IOException {
			return DatagramChannel.open();
		}
	}; 
}
