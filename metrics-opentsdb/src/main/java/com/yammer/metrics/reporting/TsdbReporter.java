package com.yammer.metrics.reporting;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.VirtualMachineMetrics;

/**
 * @author w.deborger@gmail.com
 *
 */
public class TsdbReporter extends AbstractSocketReporter implements
		MetricProcessor<Long> {
	
	
	 /**
     * Enables the TSDB reporter to send data for the default metrics registry to TSDB
     * server with the specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     * @param host   the host name of TSDB server (carbon-cache agent)
     * @param port   the port number on which the TSDB server is listening
     */
    public static void enable(long period, TimeUnit unit, String host, int port) {
        enable(Metrics.defaultRegistry(), period, unit, host, port);
    }

    /**
     * Enables the TSDB reporter to send data for the given metrics registry to TSDB server
     * with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param host            the host name of TSDB server (carbon-cache agent)
     * @param port            the port number on which the TSDB server is listening
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port) {
        enable(metricsRegistry, period, unit, host, port, null);
    }

    /**
     * Enables the TSDB reporter to send data to TSDB server with the specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     * @param host   the host name of TSDB server (carbon-cache agent)
     * @param port   the port number on which the TSDB server is listening
     * @param prefix the string which is prepended to all metric names
     */
    public static void enable(long period, TimeUnit unit, String host, int port, String prefix) {
        enable(Metrics.defaultRegistry(), period, unit, host, port, prefix);
    }

    /**
     * Enables the TSDB reporter to send data to TSDB server with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param host            the host name of TSDB server (carbon-cache agent)
     * @param port            the port number on which the TSDB server is listening
     * @param prefix          the string which is prepended to all metric names
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port, String prefix) {
        enable(metricsRegistry, period, unit, host, port, prefix, MetricPredicate.ALL);
    }

    /**
     * Enables the TSDB reporter to send data to TSDB server with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param host            the host name of TSDB server (carbon-cache agent)
     * @param port            the port number on which the TSDB server is listening
     * @param prefix          the string which is prepended to all metric names
     * @param predicate       filters metrics to be reported
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port, String prefix, MetricPredicate predicate) {
        try {
            final TsdbReporter reporter = new TsdbReporter(metricsRegistry,
                                                                   prefix,
                                                                   predicate,
                                                                   new DefaultCachingSocketProvider(host,
                                                                                             port),
                                                                   Clock.defaultClock());
            reporter.start(period, unit);
        } catch (Exception e) {
            LOG.error("Error creating/starting TSDB reporter:", e);
        }
    }

    
    

    private static final Logger LOG = LoggerFactory.getLogger(TsdbReporter.class);

	
	private String hostName;
	
	protected TsdbReporter(String host, int port) {
		this(Metrics.defaultRegistry(),host,port);
	}
	
	
	protected TsdbReporter(MetricsRegistry metricsRegistry,String host, int port) {
		this(metricsRegistry,null,MetricPredicate.ALL,new DefaultCachingSocketProvider(host,port),Clock.defaultClock(),VirtualMachineMetrics.getInstance());
		
	}

	TsdbReporter(MetricsRegistry metricsRegistry,
			String prefix, MetricPredicate predicate,
			SocketProvider socketProvider, Clock clock,String hostname) {
		super(metricsRegistry,prefix,predicate,socketProvider,clock,VirtualMachineMetrics.getInstance(),"TsdbReporter");
		this.hostName = hostname;
	}

	
	
	protected TsdbReporter(MetricsRegistry metricsRegistry,
			String prefix, MetricPredicate predicate,
			SocketProvider socketProvider, Clock clock,
			VirtualMachineMetrics vm) {
		super(metricsRegistry,prefix,predicate,socketProvider,clock,vm,"TsdbReporter");
		try {
			this.hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOG.error("could not get hostname", e);
			throw new Error(e);
		}
	}

	public TsdbReporter(MetricsRegistry registry, String prefix,
			MetricPredicate all, SocketProvider socketProvider, Clock clock) {
		this(registry,prefix,all,socketProvider,clock,VirtualMachineMetrics.getInstance());
	}


	private String makeName(MetricName metricName, String valueName) {
		return prefix+String.format("%s.%s.%s",metricName.getGroup(),metricName.getType(),valueName);
	}
	

	private String makeTags(MetricName metricName) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("name=");
		sb.append(metricName.getName());
		
		sb.append(" host=");
		sb.append(hostName);
		
		if(metricName.hasScope()){
			sb.append(" scope=");
			sb.append(metricName.getScope());
		}
		return sb.toString();
	}
	
	@Override
	public void sendFloat(long timestamp, MetricName metricName,
			String valueName, double value) throws IOException {
		String name = makeName(metricName,valueName);
		String tags = makeTags(metricName);
		send(String.format("put %s %d %f %s",name, timestamp,value,tags));
	}

	


	private void send(String format) throws IOException {
		writer.write(format);
		writer.write('\n');
	}

	@Override
	public void sendInt(long timestamp, MetricName metricName,
			String valueName, long value) throws IOException {
		String name = makeName(metricName,valueName);
		String tags = makeTags(metricName);
		send(String.format("put %s %d %d %s",name, timestamp,value,tags));
	}

	@Override
	public void sendString(long timestamp, MetricName metricName,
			String valueName, String value) throws IOException {
		String name = makeName(metricName,valueName);
		String tags = makeTags(metricName);
		send(String.format("put %s %d %s %s",name, timestamp,value,tags));
	}

	

}
