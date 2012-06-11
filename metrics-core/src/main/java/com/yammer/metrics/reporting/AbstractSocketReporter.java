package com.yammer.metrics.reporting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.Thread.State;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Sampling;
import com.yammer.metrics.core.Summarizable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.stats.Snapshot;

/**
 * @author w.deborger@gmail.com
 * 
 * derived from metrics-ganglia
 */
public abstract class AbstractSocketReporter extends AbstractPollingReporter
		implements MetricProcessor<Long> {

	private static final Logger LOG = LoggerFactory
			.getLogger(AbstractSocketReporter.class);

	protected final String prefix;
	protected final MetricPredicate predicate;
	protected final Locale locale = Locale.US;
	protected final Clock clock;
	protected final SocketProvider socketProvider;
	protected final VirtualMachineMetrics vm;
	public boolean printVMMetrics = true;

	/**
	 * Creates a new {@link GraphiteReporter}.
	 * 
	 * @param metricsRegistry
	 *            the metrics registry
	 * @param prefix
	 *            is prepended to all names reported to graphite
	 * @param predicate
	 *            filters metrics to be reported
	 * @param socketProvider
	 *            a {@link SocketProvider} instance
	 * @param clock
	 *            a {@link Clock} instance
	 * @param vm
	 *            a {@link VirtualMachineMetrics} instance
	 * @throws IOException
	 *             if there is an error connecting to the Graphite server
	 */
	public AbstractSocketReporter(MetricsRegistry metricsRegistry,
			String prefix, MetricPredicate predicate,
			SocketProvider socketProvider, Clock clock,
			VirtualMachineMetrics vm, String name){
		super(metricsRegistry, name);
		this.socketProvider = socketProvider;
		this.vm = vm;

		this.clock = clock;

		if (prefix != null) {
			// Pre-append the "." so that we don't need to make anything
			// conditional later.
			this.prefix = prefix + ".";
		} else {
			this.prefix = "";
		}
		this.predicate = predicate;
	}

	@Override
	public void shutdown() {
		try {
			socketProvider.get().close();
		} catch (Exception e) {
			LOG.warn("Error closing socket", e);
		}
		super.shutdown();
	}

	BufferedWriter writer = null;
	long lastEpoch = -1;
	
	@Override
	public synchronized void run() {
		//FIXME passing epoch on stack and writer via field, messy
		Socket socket = null;
		try {
			socket = this.socketProvider.get();
			writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			final long epoch = clock.getTime() / 1000;
			if (this.printVMMetrics) {
				printVmMetrics(epoch);
			}
			printRegularMetrics(epoch);
			writer.flush();
			lastEpoch = epoch;
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Error writing to socket", e);
			} else {
				LOG.warn("Error writing to socket: {}", e.getMessage());
			}
			if (writer != null) {
				try {
					writer.flush();
				} catch (IOException e1) {
					LOG.error("Error while flushing writer:", e1);
				}
			}
		}
	}

	protected void printRegularMetrics(final Long epoch) {
		for (Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry()
				.getGroupedMetrics(predicate).entrySet()) {
			for (Entry<MetricName, Metric> subEntry : entry.getValue()
					.entrySet()) {
				final Metric metric = subEntry.getValue();
				if (metric != null) {
					try {
						metric.processWith(this, subEntry.getKey(), epoch);
					} catch (Exception ignored) {
						LOG.error("Error printing regular metrics:", ignored);
					}
				}
			}
		}
	}

	protected void printVmMetrics(long epoch) throws IOException {
		sendFloat(epoch, new MetricName("jvm","memory","heap"),"usage", vm.getHeapUsage());
		sendFloat(epoch, new MetricName("jvm","memory","non_heap"),"usage", vm.getNonHeapUsage());
		for (Entry<String, Double> pool : vm.getMemoryPoolUsage().entrySet()) {
			sendFloat(epoch, new MetricName("jvm","memory_pool",sanitizeString(pool.getKey())),"usage", pool.getValue());
		}

		sendInt(epoch, new MetricName("jvm","threads", "daemon_thread"),"count", vm.getDaemonThreadCount());
		sendInt(epoch, new MetricName("jvm","threads", "thread"),"count", vm.getThreadCount());
		sendInt(epoch, new MetricName("jvm","uptime","uptime"),"time", vm.getUptime());
		sendFloat(epoch, new MetricName("jvm","fd","fd"),"usage", vm.getFileDescriptorUsage());

		for (Entry<State, Double> entry : vm.getThreadStatePercentages()
				.entrySet()) {
			sendFloat(epoch,new MetricName("jvm","thread-states", entry.getKey().toString()
					.toLowerCase()),"percent", entry.getValue());
		}

		for (Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm
				.getGarbageCollectors().entrySet()) {
			sendInt(epoch, new MetricName("jvm","gc",sanitizeString(entry.getKey())),
					"time",entry.getValue().getTime(TimeUnit.MILLISECONDS));
			sendInt(epoch, new MetricName("jvm","gc",sanitizeString(entry.getKey())),"count", entry.getValue().getRuns());
		}
	}

	public abstract void sendFloat(long timestamp, MetricName metricName,String valueName, double value) throws IOException ;
	public abstract void sendInt(long timestamp, MetricName metricName,String valueName, long value) throws IOException ;
	public abstract void sendString(long timestamp, MetricName metricName,String valueName, String value) throws IOException ;
	public void sendObj(long timestamp,  MetricName metricName,String valueName, Object value) throws IOException {
	        sendString(timestamp, metricName, valueName,String.format(locale, "%s", value));
	}


	protected String sanitizeName(MetricName name) {
		final StringBuilder sb = new StringBuilder().append(name.getGroup())
				.append('.').append(name.getType()).append('.');
		if (name.hasScope()) {
			sb.append(name.getScope()).append('.');
		}
		return sb.append(name.getName()).toString();
	}

	protected String sanitizeString(String s) {
		return s.replace(' ', '-');
	}

	@Override
	public void processGauge(MetricName name, Gauge<?> gauge, Long epoch)
			throws IOException {
		sendObj(epoch, name,"value", gauge.getValue());
	}

	@Override
	public void processCounter(MetricName name, Counter counter, Long epoch)
			throws IOException {
		sendInt(epoch,name, "count", counter.getCount());
	}

	@Override
	public void processMeter(MetricName name, Metered meter, Long epoch)
			throws IOException {
		sendInt(epoch, name, "count", meter.getCount());
		sendFloat(epoch, name, "meanRate", meter.getMeanRate());
		sendFloat(epoch, name, "1MinuteRate", meter.getOneMinuteRate());
		sendFloat(epoch, name, "5MinuteRate",
				meter.getFiveMinuteRate());
		sendFloat(epoch, name, "15MinuteRate",
				meter.getFifteenMinuteRate());
	}

	@Override
	public void processHistogram(MetricName name, Histogram histogram,
			Long epoch) throws IOException {
		sendSummarizable(epoch, name, histogram);
		sendSampling(epoch, name, histogram);
	}

	@Override
	public void processTimer(MetricName name, Timer timer, Long epoch)
			throws IOException {
		processMeter(name, timer, epoch);
		getInstantMean(name,timer,epoch);
		
		sendSummarizable(epoch, name, timer);
		sendSampling(epoch, name, timer);
	}

	
	protected void sendSummarizable(long epoch, MetricName sanitizedName,
			Summarizable metric) throws IOException {
		sendFloat(epoch, sanitizedName, "min", metric.getMin());
		sendFloat(epoch, sanitizedName, "max", metric.getMax());
		sendFloat(epoch, sanitizedName, "mean", metric.getMean());
		sendFloat(epoch, sanitizedName, "stddev", metric.getStdDev());
	}

	protected void sendSampling(long epoch, MetricName sanitizedName,
			Sampling metric) throws IOException {
		final Snapshot snapshot = metric.getSnapshot();
		sendFloat(epoch, sanitizedName, "median", snapshot.getMedian());
		sendFloat(epoch, sanitizedName, "75percentile",
				snapshot.get75thPercentile());
		sendFloat(epoch, sanitizedName, "95percentile",
				snapshot.get95thPercentile());
		sendFloat(epoch, sanitizedName, "98percentile",
				snapshot.get98thPercentile());
		sendFloat(epoch, sanitizedName, "99percentile",
				snapshot.get99thPercentile());
		sendFloat(epoch, sanitizedName, "999percentile",
				snapshot.get999thPercentile());
	}
	
	
	private Map<Timer,Double> last = new HashMap<Timer, Double>();
	
	private void getInstantMean(MetricName name, Timer timer, Long epoch) throws IOException {
		Double prevV = last.get(timer);
		if(prevV == null){
			last.put(timer,timer.getSum());
			return;
		}
		
		double timeDelta = epoch-lastEpoch;
		if(timeDelta == 0)
			return;
		
		double newV = timer.getSum();
		last.put(timer,newV);
		sendFloat(epoch,name,"instantMean",(newV-prevV)/timeDelta); 
		
	}

}
