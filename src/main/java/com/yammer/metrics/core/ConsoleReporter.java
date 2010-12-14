package com.yammer.metrics.core;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple reporters which prints out application metrics to a
 * {@link PrintStream} periodically.
 *
 * @author coda
 */
public class ConsoleReporter implements Runnable {
	private static final ScheduledExecutorService TICK_THREAD =
			Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("meter-tick"));
	private final PrintStream out;
	private final Map<MetricName,Metric> metrics;
	private ScheduledFuture<?> future;

	/**
	 * Creates a new {@link ConsoleReporter}.
	 *
	 * @param metrics a map of {@link MetricName}s to {@link Metric}s
	 * @param out the {@link PrintStream} to which output will be written
	 */
	/*package*/ ConsoleReporter(Map<MetricName, Metric> metrics, PrintStream out) {
		this.metrics = metrics;
		this.out = out;
	}

	/**
	 * Starts printing output to the specified {@link PrintStream}.
	 *
	 * @param period the period between successive displays
	 * @param unit the time unit of {@code period}
	 */
	public void start(long period, TimeUnit unit) {
		this.future = TICK_THREAD.scheduleAtFixedRate(this, period, period, unit);
	}

	/**
	 * Stops printing to the console.
	 */
	public void stop() {
		if (future != null) {
			future.cancel(true);
			future = null;
		}
	}

	private SortedMap<String, SortedMap<String, Metric>> sortedMetrics() {
		final SortedMap<String, SortedMap<String, Metric>> sortedMetrics =
				new TreeMap<String, SortedMap<String, Metric>>();
		for (Entry<MetricName, Metric> entry : metrics.entrySet()) {
			final String packageName = entry.getKey().getKlass().getCanonicalName();
			SortedMap<String, Metric> submetrics = sortedMetrics.get(packageName);
			if (submetrics == null) {
				submetrics = new TreeMap<String, Metric>();
				sortedMetrics.put(packageName, submetrics);
			}
			submetrics.put(entry.getKey().getName(), entry.getValue());
		}
		return sortedMetrics;
	}

	@Override
	public void run() {
		try {
			final DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
			final String dateTime = format.format(new Date());
			out.print(dateTime);
			out.print(' ');
			for (int i = 0; i < (80 - dateTime.length() - 1); i++) {
				out.print('=');
			}
			out.println();

			for (Entry<String, SortedMap<String, Metric>> entry : sortedMetrics().entrySet()) {
				out.print(entry.getKey());
				out.println(':');

				for (Entry<String, Metric> subEntry : entry.getValue().entrySet()) {
					out.print("  ");
					out.print(subEntry.getKey());
					out.println(':');

					final Metric metric = subEntry.getValue();
					if (metric instanceof ValueMetric<?>) {
						printValue((ValueMetric) metric);
					} else if (metric instanceof CounterMetric) {
						printCounter((CounterMetric) metric);
					} else if (metric instanceof MeterMetric) {
						printMeter((MeterMetric) metric);
					} else if (metric instanceof TimerMetric) {
						printTimer((TimerMetric) metric);
					}
					out.println();
				}
				out.println();
			}
			out.println();
			out.flush();
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}

	private void printValue(ValueMetric metric) {
		final ValueMetric valueMetric = metric;
		out.print("    value = ");
		out.println(valueMetric.value());
	}

	private void printCounter(CounterMetric metric) {
		final CounterMetric counter = metric;
		out.print("    count = ");
		out.println(counter.count());
	}

	private void printMeter(MeterMetric metric) {
		final MeterMetric meter = metric;
		final String unit = abbrev(meter.getUnit());
		out.printf("             count = %d\n", meter.count());
		out.printf("         mean rate = %2.2f events/%s\n", meter.meanRate(), unit);
		out.printf("     1-minute rate = %2.2f events/%s\n", meter.oneMinuteRate(), unit);
		out.printf("     5-minute rate = %2.2f events/%s\n", meter.fiveMinuteRate(), unit);
		out.printf("    15-minute rate = %2.2f events/%s\n", meter.fifteenMinuteRate(), unit);
	}

	private void printTimer(TimerMetric metric) {
		final TimerMetric timer = metric;
		final String rateUnit = abbrev(timer.getRateUnit());
		final String latencyUnit = abbrev(timer.getLatencyUnit());

		out.printf("             count = %d\n", timer.count());
		out.printf("         mean rate = %2.2f events/%s\n", timer.meanRate(), rateUnit);
		out.printf("     1-minute rate = %2.2f events/%s\n", timer.oneMinuteRate(), rateUnit);
		out.printf("     5-minute rate = %2.2f events/%s\n", timer.fiveMinuteRate(), rateUnit);
		out.printf("    15-minute rate = %2.2f events/%s\n", timer.fifteenMinuteRate(), rateUnit);

		final double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
		out.printf("               min = %2.2f%s\n", timer.min(), latencyUnit);
		out.printf("               max = %2.2f%s\n", timer.max(), latencyUnit);
		out.printf("              mean = %2.2f%s\n", timer.mean(), latencyUnit);
		out.printf("            stddev = %2.2f%s\n", timer.stdDev(), latencyUnit);
		out.printf("            median = %2.2f%s\n", percentiles[0], latencyUnit);
		out.printf("              75%% <= %2.2f%s\n", percentiles[1], latencyUnit);
		out.printf("              95%% <= %2.2f%s\n", percentiles[2], latencyUnit);
		out.printf("              98%% <= %2.2f%s\n", percentiles[3], latencyUnit);
		out.printf("              99%% <= %2.2f%s\n", percentiles[4], latencyUnit);
		out.printf("            99.9%% <= %2.2f%s\n", percentiles[5], latencyUnit);
	}

	private String abbrev(TimeUnit unit) {
		switch (unit) {
			case NANOSECONDS:
				return "ns";
			case MICROSECONDS:
				return "us";
			case MILLISECONDS:
				return "ms";
			case SECONDS:
				return "s";
			case MINUTES:
				return "m";
			case HOURS:
				return "h";
			case DAYS:
				return "d";
		}
		throw new IllegalArgumentException("Unrecognized TimeUnit: " + unit);
	}
}
