package com.codahale.metrics.chukwa;

import java.net.Socket;
import java.util.SortedMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SocketAppender;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

/**
 * A reporter which outputs measurements to a {@link Socket},
 */
public class SocketReporter extends ScheduledReporter {
	/**
	 * Returns a new {@link Builder} for {@link SocketReporter}.
	 * 
	 * @param registry
	 *            the registry to report
	 * @return a {@link Builder} instance for a {@link SocketReporter}
	 */
	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	/**
	 * A builder for {@link SocketReporter} instances. Defaults to socket to
	 * {@code metrics}, converting rates to events/second, converting durations
	 * to milliseconds, and not filtering metrics.
	 */
	public static class Builder {
		private final MetricRegistry registry;
		private String host;
		private int port;
		private TimeUnit rateUnit;
		private TimeUnit durationUnit;
		private MetricFilter filter;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
			this.rateUnit = TimeUnit.SECONDS;
			this.durationUnit = TimeUnit.MILLISECONDS;
			this.filter = MetricFilter.ALL;
		}

		/**
		 * Use the host for sending the metrics 
		 * 
		 * @param host
		 * @return {@code this}
		 */
		public Builder withHost(String host) {
			this.host = host;
			return this;
		}

		/**
		 * Use the port for sending the metrics
		 * 
		 * @param port
		 * @return {@code this}
		 */
		public Builder withPort(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Convert rates to the given time unit.
		 * 
		 * @param rateUnit
		 *            a unit of time
		 * @return {@code this}
		 */
		public Builder convertRatesTo(TimeUnit rateUnit) {
			this.rateUnit = rateUnit;
			return this;
		}

		/**
		 * Convert durations to the given time unit.
		 * 
		 * @param durationUnit
		 *            a unit of time
		 * @return {@code this}
		 */
		public Builder convertDurationsTo(TimeUnit durationUnit) {
			this.durationUnit = durationUnit;
			return this;
		}

		/**
		 * Only report metrics which match the given filter.
		 * 
		 * @param filter
		 *            a {@link MetricFilter}
		 * @return {@code this}
		 */
		public Builder filter(MetricFilter filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Builds a {@link SocketReporter} with the given properties.
		 * 
		 * @return a {@link SocketReporter}
		 */
		public SocketReporter build() {
			return new SocketReporter(registry, host, port, rateUnit,
					durationUnit, filter);
		}
	}

	private final String host;
	private final int port;
	private final SocketAppender appender;
	private final Logger log;
	private final PatternLayout layout;

	private SocketReporter(MetricRegistry registry, String host, int port,
			TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter) {
		super(registry, "socket-reporter", filter, rateUnit, durationUnit);
		this.host = host;
		this.port = port;
		
		appender = new SocketAppender();
		log = Logger.getLogger("Metrics");
		layout = new PatternLayout("%m%n");
		
		appender.setLayout(layout);
		log.addAppender(appender);
	}

	@Override
	public void report(SortedMap<String, Gauge> gauges,
			SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms,
			SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {

		appender.setRemoteHost(host);
		appender.setPort(port);
		appender.activateOptions();
		
		if (gauges != null) {
			for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
				writeGauge(entry.getKey(), entry.getValue());
			}
		}

		if (counters != null) {
			for (Map.Entry<String, Counter> entry : counters.entrySet()) {
				writeCounter(entry.getKey(), entry.getValue());
			}
		}

		if (histograms != null) {
			for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
				writeHistogram(entry.getKey(), entry.getValue());
			}
		}

		if (meters != null) {
			for (Map.Entry<String, Meter> entry : meters.entrySet()) {
				writeMeter(entry.getKey(), entry.getValue());
			}
		}

		if (timers != null) {
			for (Map.Entry<String, Timer> entry : timers.entrySet()) {
				writeTimer(entry.getKey(), entry.getValue());
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void writeGauge(String key, Gauge gauge) {
		final JSONObject json = new JSONObject();
		
		json.put("type", "gauge");
		json.put("key", key);
		json.put("value", gauge.getValue());

		log.info(json.toString());
	}

	@SuppressWarnings("unchecked")
	private void writeCounter(String key, Counter counter) {
		final JSONObject json = new JSONObject();
		
		json.put("type", "counter");
		json.put("key", key);
		json.put("value", counter.getCount());

		log.info(json.toString());
	}

	@SuppressWarnings("unchecked")
	private void writeHistogram(String key, Histogram histogram) {
		final Snapshot snapshot = histogram.getSnapshot();
		final JSONObject json = new JSONObject();
		
		json.put("type", "histogram");
		json.put("key", key);
		json.put("count", histogram.getCount());
		json.put("min", snapshot.getMin());
		json.put("max", snapshot.getMax());
		json.put("mean", snapshot.getMean());
		json.put("stddev", snapshot.getStdDev());
		json.put("median", snapshot.getMedian());
		json.put("p75", snapshot.get75thPercentile());
		json.put("p95", snapshot.get95thPercentile());
		json.put("p98", snapshot.get98thPercentile());
		json.put("p99", snapshot.get99thPercentile());
		json.put("p999", snapshot.get999thPercentile());
		
		log.info(json.toString());
	}

	@SuppressWarnings("unchecked")
	private void writeMeter(String key, Meter meter) {
		final JSONObject json = new JSONObject();
		
		json.put("type", "meter");
		json.put("key", key);
		json.put("count", meter.getCount());
		json.put("mean_rate", meter.getMeanRate());
		json.put("m1", meter.getOneMinuteRate());
		json.put("m5", meter.getFiveMinuteRate());
		json.put("m15", meter.getFifteenMinuteRate());
		json.put("rate_unit", getRateUnit());
		
		log.info(json.toString());
	}

	@SuppressWarnings("unchecked")
	private void writeTimer(String key, Timer timer) {
		final Snapshot snapshot = timer.getSnapshot();
		final JSONObject json = new JSONObject();

		json.put("type", "timer");
		json.put("key", key);
		json.put("count", convertDuration(timer.getCount()));
		json.put("min", convertDuration(snapshot.getMin()));
		json.put("max", convertDuration(snapshot.getMax()));
		json.put("mean", convertDuration(snapshot.getMean()));
		json.put("stddev", convertDuration(snapshot.getStdDev()));
		json.put("median", convertDuration(snapshot.getMedian()));
		json.put("p75", convertDuration(snapshot.get75thPercentile()));
		json.put("p95", convertDuration(snapshot.get95thPercentile()));
		json.put("p98", convertDuration(snapshot.get98thPercentile()));
		json.put("p99", convertDuration(snapshot.get99thPercentile()));
		json.put("p999", convertDuration(snapshot.get999thPercentile()));
		json.put("mean_rate", timer.getMeanRate());
		json.put("m1", timer.getOneMinuteRate());
		json.put("m5", timer.getFiveMinuteRate());
		json.put("m15", timer.getFifteenMinuteRate());
		json.put("rate_unit", getRateUnit());
		json.put("duration_unit", getDurationUnit());
		
		log.info(json.toString());
	}
}
