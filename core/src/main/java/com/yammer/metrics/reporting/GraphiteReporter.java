package com.yammer.metrics.reporting;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.util.NamedThreadFactory;
import com.yammer.metrics.util.Utils;

/**
 * A simple reporters which sends out application metrics to a
 * Graphite server periodically. http://graphite.wikidot.com/faq
 *
 * @author Mahesh Tiyyagura <tmahesh@gmail.com>
 */
public class GraphiteReporter implements Runnable {
	private static final ScheduledExecutorService TICK_THREAD =
			Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("metrics-Graphite-reporter"));
	private final Writer writer;
	
	
	/**
	 * Creates a new {@link GraphiteReporter}.
	 *
	 * @param host is graphite server
	 * @param port is port on which graphite server is running
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public GraphiteReporter(String host,int port) throws UnknownHostException, IOException {
	    Socket socket = new Socket(host,port);
	    this.writer = new OutputStreamWriter(socket.getOutputStream());
	}

	/**
	 * Starts sending output to graphite server.
	 *
	 * @param period the period between successive displays
	 * @param unit the time unit of {@code period}
	 */
	public void start(long period, TimeUnit unit) {
		TICK_THREAD.scheduleAtFixedRate(this, period, period, unit);
	}

	@Override
	public void run() {
		try {
		    long epoch = System.currentTimeMillis()/1000;			
			for (Entry<String, Map<String, Metric>> entry : Utils.sortMetrics(Metrics.allMetrics()).entrySet()) {
				for (Entry<String, Metric> subEntry : entry.getValue().entrySet()) {
                    			final String simpleName = entry.getKey()+"."+subEntry.getKey();
					final Metric metric = subEntry.getValue();
					if (metric != null) {
						try {
		                    if (metric instanceof GaugeMetric<?>) {
								printGauge((GaugeMetric<?>) metric, simpleName, epoch);
							} else if (metric instanceof CounterMetric) {
								printCounter((CounterMetric) metric, simpleName, epoch);
							} else if (metric instanceof HistogramMetric) {
								printHistogram((HistogramMetric) metric, simpleName, epoch);
							} else if (metric instanceof MeterMetric) {
								printMetered((MeterMetric) metric, simpleName, epoch);
							} else if (metric instanceof TimerMetric) {
								printTimer((TimerMetric) metric, simpleName, epoch);
							}
						} catch (Exception ignored) {
							ignored.printStackTrace();
						}
					}
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
			
	}
	
	private void sendToGraphite(StringBuffer line){
		try {
			writer.write(line.toString());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printGauge(GaugeMetric<?> gauge, String name, long epoch) {
		StringBuffer line = new StringBuffer();
		line.append(String.format("%s.%s %d %d\n", name,"value",gauge.value(), epoch));
		sendToGraphite(line);
	}

	private void printCounter(CounterMetric counter, String name, long epoch) {
		StringBuffer line = new StringBuffer();
		line.append(String.format("%s.%s %d %d\n", name,"count",counter.count(), epoch));
		sendToGraphite(line);
	}

	private void printMetered(Metered meter, String name, long epoch) {
		StringBuffer line = new StringBuffer();
		line.append(String.format("%s.%s %d %d\n",    name,"count",meter.count(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"meanRate",meter.meanRate(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"1MinuteRate",meter.oneMinuteRate(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"5MinuteRate",meter.fiveMinuteRate(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"15MinuteRate",meter.fifteenMinuteRate(), epoch));
		sendToGraphite(line);
		
	}

	private void printHistogram(HistogramMetric histogram, String name, long epoch) {
		final double[] percentiles = histogram.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
		StringBuffer line = new StringBuffer();
		line.append(String.format("%s.%s %2.2f %d\n", name,"min",histogram.min(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"max",histogram.max(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"mean",histogram.mean(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"stddev",histogram.stdDev(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"median",percentiles[0], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"75percentile",percentiles[1], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"95percentile",percentiles[2], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"98percentile",percentiles[3], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"99percentile",percentiles[4], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"999percentile",percentiles[5], epoch));
		
		sendToGraphite(line);
	}

	private void printTimer(TimerMetric timer, String name, long epoch) {
		printMetered(timer, name, epoch);

		final double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
		
		StringBuffer line = new StringBuffer();
		line.append(String.format("%s.%s %2.2f %d\n", name,"min",timer.min(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"max",timer.max(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"mean",timer.mean(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"stddev",timer.stdDev(), epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"median",percentiles[0], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"75percentile",percentiles[1], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"95percentile",percentiles[2], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"98percentile",percentiles[3], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"99percentile",percentiles[4], epoch));
		line.append(String.format("%s.%s %2.2f %d\n", name,"999percentile",percentiles[5], epoch));		
		sendToGraphite(line);
	}

}
