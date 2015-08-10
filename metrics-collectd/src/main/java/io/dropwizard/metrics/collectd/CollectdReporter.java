package io.dropwizard.metrics.collectd;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.dropwizard.metrics.collectd.part.Plugin;
import io.dropwizard.metrics.collectd.part.PluginInstance;
import io.dropwizard.metrics.collectd.part.Time;
import io.dropwizard.metrics.collectd.part.Type;
import io.dropwizard.metrics.collectd.part.TypeInstance;
import io.dropwizard.metrics.collectd.part.Values;

/**
 * A reporter which publishes metric values to a Collectd server using the
 * collectd binary protocol over UDP.
 *
 * @see <a href="https://collectd.org/wiki/index.php/Binary_protocol">collectd -
 *      Binary protocol</a>
 */
public class CollectdReporter extends ScheduledReporter {
	/**
	 * Returns a new {@link Builder} for {@link CollectdReporter}.
	 *
	 * @param registry
	 *            the registry to report
	 * @return a {@link Builder} instance for a {@link CollectdReporter}
	 */
	public static Builder forRegistry(final MetricRegistry registry) {
		return new Builder(registry);
	}

	/**
	 * A builder for {@link CollectdReporter} instances. Defaults to using a
	 * plugin instance of "dropwizard", using the default clock, converting
	 * rates to events/second, converting durations to milliseconds, and not
	 * filtering metrics.
	 */
	public static class Builder {
		private static final String DEFAULT_PLUGIN_INSTANCE = "dropwizard";

		private final MetricRegistry registry;
		private Clock clock;
		private String pluginInstance;
		private TimeUnit rateUnit;
		private TimeUnit durationUnit;
		private MetricFilter filter;

		private Builder(final MetricRegistry registry) {
			this.registry = registry;
			clock = Clock.defaultClock();
			pluginInstance = DEFAULT_PLUGIN_INSTANCE;
			rateUnit = TimeUnit.SECONDS;
			durationUnit = TimeUnit.MILLISECONDS;
			filter = MetricFilter.ALL;
		}

		/**
		 * Use the given {@link Clock} instance for the time.
		 *
		 * @param clock
		 *            a {@link Clock} instance
		 * @return {@code this}
		 */
		public Builder withClock(final Clock clock) {
			this.clock = clock;
			return this;
		}

		/**
		 * Report all metrics as part of this plugin instance.
		 *
		 * @param pluginInstance
		 *            the plugin instance for all metrics
		 * @return {@code this}
		 */
		public Builder withPluginInstance(final String pluginInstance) {
			this.pluginInstance = pluginInstance;
			return this;
		}

		/**
		 * Convert rates to the given time unit.
		 *
		 * @param rateUnit
		 *            a unit of time
		 * @return {@code this}
		 */
		public Builder convertRatesTo(final TimeUnit rateUnit) {
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
		public Builder convertDurationsTo(final TimeUnit durationUnit) {
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
		public Builder filter(final MetricFilter filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Builds a {@link CollectdReporter} with the given properties, sending
		 * metrics using the given {@link CollectdSender}.
		 *
		 * @param collectd
		 *            a {@link CollectdSender}
		 * @return a {@link CollectdReporter}
		 */
		public CollectdReporter build(final CollectdSender collectd) {
			return new CollectdReporter(registry, collectd, clock, pluginInstance, rateUnit, durationUnit, filter);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectdReporter.class);
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	private static final Part METRICS_PLUGIN = new Plugin("metrics", UTF_8);

	private final CollectdSender collectd;
	private final Clock clock;
	private final PluginInstance pluginInstance;

	private CollectdReporter(final MetricRegistry registry, final CollectdSender collectd, final Clock clock,
			final String pluginInstance, final TimeUnit rateUnit, final TimeUnit durationUnit,
			final MetricFilter filter) {
		super(registry, "collectd-reporter", filter, rateUnit, durationUnit);
		this.collectd = collectd;
		this.clock = clock;
		this.pluginInstance = new PluginInstance(pluginInstance, UTF_8);
	}

	@Override
	public void report(@SuppressWarnings("rawtypes") final SortedMap<MetricName, Gauge> gauges,
			final SortedMap<MetricName, Counter> counters, final SortedMap<MetricName, Histogram> histograms,
			final SortedMap<MetricName, Meter> meters, final SortedMap<MetricName, Timer> timers) {
		final Time time = new Time(clock.getTime() / 1000);

		try {
			if (!collectd.isConnected()) {
				collectd.connect();
			}

			for (@SuppressWarnings("rawtypes")
			final Map.Entry<MetricName, Gauge> entry : gauges.entrySet()) {
				reportGauge(entry.getKey(), entry.getValue(), time);
			}

			for (final Map.Entry<MetricName, Counter> entry : counters.entrySet()) {
				reportCounter(entry.getKey(), entry.getValue(), time);
			}

			for (final Map.Entry<MetricName, Histogram> entry : histograms.entrySet()) {
				reportHistogram(entry.getKey(), entry.getValue(), time);
			}

			for (final Map.Entry<MetricName, Meter> entry : meters.entrySet()) {
				reportMetered(entry.getKey(), entry.getValue(), time);
			}

			for (final Map.Entry<MetricName, Timer> entry : timers.entrySet()) {
				reportTimer(entry.getKey(), entry.getValue(), time);
			}

			collectd.flush();
		} catch (final Exception e) {
			LOGGER.warn("Unable to report to Collectd", collectd, e);
			closeCollectdConnection();
		}
	}

	private void closeCollectdConnection() {
		try {
			collectd.close();
		} catch (final IOException e) {
			LOGGER.warn("Error closing Collectd", collectd, e);
		}
	}

	@Override
	public void stop() {
		try {
			super.stop();
		} finally {
			try {
				collectd.close();
			} catch (final IOException e) {
				LOGGER.debug("Error disconnecting from Collectd", collectd, e);
			}
		}
	}

	private void reportTimer(final MetricName name, final Timer timer, final Time time) throws IOException {
		send(time, "timer", name, toValues(timer));
		reportMetered(name, timer, time);
	}

	private static Value[] toValues(final Timer t) {
		final Snapshot s = t.getSnapshot();
		return new Value[] { new io.dropwizard.metrics.collectd.value.Gauge(s.getMax()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.getMean()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.getMin()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.getStdDev()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.getMedian()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get75thPercentile()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get95thPercentile()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get98thPercentile()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get99thPercentile()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get999thPercentile()) };
	}

	private void reportMetered(final MetricName name, final Metered metered, final Time time) throws IOException {
		send(time, "metered", name, toValues(metered));
	}

	private static Value[] toValues(final Metered m) {
		return new Value[] { new io.dropwizard.metrics.collectd.value.Gauge(m.getCount()),
				new io.dropwizard.metrics.collectd.value.Gauge(m.getOneMinuteRate()),
				new io.dropwizard.metrics.collectd.value.Gauge(m.getFiveMinuteRate()),
				new io.dropwizard.metrics.collectd.value.Gauge(m.getFifteenMinuteRate()),
				new io.dropwizard.metrics.collectd.value.Gauge(m.getMeanRate()) };
	}

	private void reportHistogram(final MetricName name, final Histogram histogram, final Time time) throws IOException {
		send(time, "histogram", name, toValues(histogram));
	}

	private static Value[] toValues(final Histogram h) {
		final Snapshot s = h.getSnapshot();
		return new Value[] { new io.dropwizard.metrics.collectd.value.Gauge(h.getCount()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.getMax()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.getMean()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.getMin()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.getStdDev()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.getMedian()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get75thPercentile()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get95thPercentile()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get98thPercentile()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get99thPercentile()),
				new io.dropwizard.metrics.collectd.value.Gauge(s.get999thPercentile()) };
	}

	private void reportCounter(final MetricName name, final Counter counter, final Time time) throws IOException {
		send(time, "count", name, new io.dropwizard.metrics.collectd.value.Counter(counter.getCount()));
	}

	private void reportGauge(final MetricName name, final Gauge<?> gauge, final Time time) throws IOException {
		final Double value = getValue(gauge);
		if (value != null) {
			send(time, name, new io.dropwizard.metrics.collectd.value.Gauge(value));
		}
	}

	private void send(final Time time, final MetricName typeInstance, final Value value) throws IOException {
		collectd.send(new Packet(asList(METRICS_PLUGIN, pluginInstance, time,
				new TypeInstance(typeInstance.toString(), UTF_8), new Values(asList(value)))));
	}

	private void send(final Time time, final String type, final MetricName typeInstance, final Value... values)
			throws IOException {
		collectd.send(new Packet(asList(METRICS_PLUGIN, pluginInstance, time, new Type(type, UTF_8),
				new TypeInstance(typeInstance.toString(), UTF_8), new Values(asList(values)))));
	}

	private Double getValue(final Gauge<?> g) {
		final Object o = g.getValue();
		return o instanceof Number ? ((Number) o).doubleValue() : null;
	}
}
