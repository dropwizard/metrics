package io.dropwizard.metrics.collectd;

import io.dropwizard.metrics.Clock;
import io.dropwizard.metrics.Counter;
import io.dropwizard.metrics.Gauge;
import io.dropwizard.metrics.Histogram;
import io.dropwizard.metrics.Meter;
import io.dropwizard.metrics.Metered;
import io.dropwizard.metrics.MetricFilter;
import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.ScheduledReporter;
import io.dropwizard.metrics.Snapshot;
import io.dropwizard.metrics.Timer;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reporter which publishes metric values to a Collectd server.
 * 
 * @see <a href="https://collectd.org">collectd – The system statistics
 *      collection daemon</a>
 */
public class CollectdReporter extends ScheduledReporter {

	/**
	 * Returns a new {@link Builder} for {@link CollectdReporter}.
	 *
	 * @param registry
	 *            the registry to report
	 * @return a {@link Builder} instance for a {@link CollectdReporter}
	 */
	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	/**
	 * A builder for {@link CollectdReporter} instances. Defaults to not using a
	 * prefix, using the default clock, converting rates to events/second,
	 * converting durations to milliseconds, and not filtering metrics.
	 */
	public static class Builder {
		private final MetricRegistry registry;
		private Clock clock;
		private TimeUnit rateUnit;
		private TimeUnit durationUnit;
		private MetricFilter filter;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
			this.clock = Clock.defaultClock();
			this.rateUnit = TimeUnit.SECONDS;
			this.durationUnit = TimeUnit.MILLISECONDS;
			this.filter = MetricFilter.ALL;
		}

		/**
		 * Use the given {@link Clock} instance for the time.
		 *
		 * @param clock
		 *            a {@link Clock} instance
		 * @return {@code this}
		 */
		public Builder withClock(Clock clock) {
			this.clock = clock;
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
		 * Builds a {@link CollectdReporter} with the given properties, sending
		 * metrics using the given {@link Collectd}.
		 *
		 * @param collectd
		 *            a {@link Collectd}
		 * @return a {@link CollectdReporter}
		 */
		public CollectdReporter build(Collectd collectd) {
			return new CollectdReporter(registry, collectd, clock, rateUnit, durationUnit, filter);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectdReporter.class);
	private final Collectd collectd;
	private final Clock clock;
	private long period;

	private CollectdReporter(MetricRegistry registry, Collectd collectd, Clock clock, TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter) {
		super(registry, "collectd-reporter", filter, rateUnit, durationUnit);
		this.collectd = collectd;
		this.clock = clock;
	}

	@Override
	public void start(long period, TimeUnit unit) {
		this.period = period;
		super.start(period, unit);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void report(SortedMap<MetricName, Gauge> gauges, SortedMap<MetricName, Counter> counters, SortedMap<MetricName, Histogram> histograms, SortedMap<MetricName, Meter> meters, SortedMap<MetricName, Timer> timers) {
		final long timestamp = clock.getTime() / 1000;

		// oh it'd be lovely to use Java 7 here
		try {
			if (!collectd.isConnected()) {
				collectd.connect();
			}

			for (Map.Entry<MetricName, Gauge> entry : gauges.entrySet()) {
				reportGauge(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<MetricName, Counter> entry : counters.entrySet()) {
				reportCounter(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<MetricName, Histogram> entry : histograms.entrySet()) {
				reportHistogram(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<MetricName, Meter> entry : meters.entrySet()) {
				reportMetered(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<MetricName, Timer> entry : timers.entrySet()) {
				reportTimer(entry.getKey(), entry.getValue(), timestamp);
			}

		} catch (Throwable t) {
			LOGGER.warn("Unable to report to Collectd", collectd, t);
		}
	}

	private void reportTimer(MetricName name, Timer timer, long timestamp) {
		final Snapshot snapshot = timer.getSnapshot();

		collectd.send(name, "max", convertDuration(snapshot.getMax()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "mean", convertDuration(snapshot.getMean()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "min", convertDuration(snapshot.getMin()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "stddev", convertDuration(snapshot.getStdDev()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p50", convertDuration(snapshot.getMedian()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p75", convertDuration(snapshot.get75thPercentile()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p95", convertDuration(snapshot.get95thPercentile()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p98", convertDuration(snapshot.get98thPercentile()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p99", convertDuration(snapshot.get99thPercentile()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p999", convertDuration(snapshot.get999thPercentile()), timestamp, DataType.GAUGE, period);

		reportMetered(name, timer, timestamp);
	}

	private void reportMetered(MetricName name, Metered meter, long timestamp) {
		collectd.send(name, "count", meter.getCount(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "m1_rate", convertRate(meter.getOneMinuteRate()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "m5_rate", convertRate(meter.getFiveMinuteRate()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "m15_rate", convertRate(meter.getFifteenMinuteRate()), timestamp, DataType.GAUGE, period);
		collectd.send(name, "mean_rate", convertRate(meter.getMeanRate()), timestamp, DataType.GAUGE, period);
	}

	private void reportHistogram(MetricName name, Histogram histogram, long timestamp) {
		final Snapshot snapshot = histogram.getSnapshot();
		collectd.send(name, "count", histogram.getCount(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "max", snapshot.getMax(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "mean", snapshot.getMean(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "min", snapshot.getMin(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "stddev", snapshot.getStdDev(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p50", snapshot.getMedian(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p75", snapshot.get75thPercentile(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p95", snapshot.get95thPercentile(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p98", snapshot.get98thPercentile(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p99", snapshot.get99thPercentile(), timestamp, DataType.GAUGE, period);
		collectd.send(name, "p999", snapshot.get999thPercentile(), timestamp, DataType.GAUGE, period);
	}

	private void reportCounter(MetricName key, Counter value, long timestamp) {
		collectd.send(key, value.getCount(), timestamp, DataType.COUNTER, period);
	}

	private void reportGauge(MetricName key, Gauge<?> value, long timestamp) {
		collectd.send(key, (Number) value.getValue(), timestamp, DataType.GAUGE, period);
	}

}
