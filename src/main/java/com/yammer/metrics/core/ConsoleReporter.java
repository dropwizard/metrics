package com.yammer.metrics.core;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
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
			Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("metrics-console-reporter"));
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

			for (Entry<String, Map<String, Metric>> entry : Utils.sortMetrics(metrics).entrySet()) {
				out.print(entry.getKey());
				out.println(':');

				for (Entry<String, Metric> subEntry : entry.getValue().entrySet()) {
					out.print("  ");
					out.print(subEntry.getKey());
					out.println(':');

					final Metric metric = subEntry.getValue();
					if (metric instanceof GaugeMetric<?>) {
						printGauge((GaugeMetric) metric);
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

	private void printGauge(GaugeMetric gauge) {
		out.print("    value = ");
		out.println(gauge.value());
	}

	private void printCounter(CounterMetric counter) {
		out.print("    count = ");
		out.println(counter.count());
	}

	private void printMeter(MeterMetric meter) {
		final String unit = abbrev(meter.getScaleUnit());
		out.printf("             count = %d\n", meter.count());
		out.printf("         mean rate = %2.2f %s/%s\n", meter.meanRate(), meter.getEventType(), unit);
		out.printf("     1-minute rate = %2.2f %s/%s\n", meter.oneMinuteRate(), meter.getEventType(), unit);
		out.printf("     5-minute rate = %2.2f %s/%s\n", meter.fiveMinuteRate(), meter.getEventType(), unit);
		out.printf("    15-minute rate = %2.2f %s/%s\n", meter.fifteenMinuteRate(), meter.getEventType(), unit);
	}

	private void printTimer(TimerMetric timer) {
		final String rateUnit = abbrev(timer.getRateUnit());
		final String durationUnit = abbrev(timer.getDurationUnit());

		out.printf("             count = %d\n", timer.count());
		out.printf("         mean rate = %2.2f %s/%s\n", timer.meanRate(), timer.getEventType(), rateUnit);
		out.printf("     1-minute rate = %2.2f %s/%s\n", timer.oneMinuteRate(), timer.getEventType(), rateUnit);
		out.printf("     5-minute rate = %2.2f %s/%s\n", timer.fiveMinuteRate(), timer.getEventType(), rateUnit);
		out.printf("    15-minute rate = %2.2f %s/%s\n", timer.fifteenMinuteRate(), timer.getEventType(), rateUnit);

		final double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
		out.printf("               min = %2.2f%s\n", timer.min(), durationUnit);
		out.printf("               max = %2.2f%s\n", timer.max(), durationUnit);
		out.printf("              mean = %2.2f%s\n", timer.mean(), durationUnit);
		out.printf("            stddev = %2.2f%s\n", timer.stdDev(), durationUnit);
		out.printf("            median = %2.2f%s\n", percentiles[0], durationUnit);
		out.printf("              75%% <= %2.2f%s\n", percentiles[1], durationUnit);
		out.printf("              95%% <= %2.2f%s\n", percentiles[2], durationUnit);
		out.printf("              98%% <= %2.2f%s\n", percentiles[3], durationUnit);
		out.printf("              99%% <= %2.2f%s\n", percentiles[4], durationUnit);
		out.printf("            99.9%% <= %2.2f%s\n", percentiles[5], durationUnit);
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
