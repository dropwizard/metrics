package com.yammer.metrics.reporting;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.util.NamedThreadFactory;
import com.yammer.metrics.util.Utils;

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

	/**
	 * Creates a new {@link ConsoleReporter}.
	 *
	 * @param out the {@link java.io.PrintStream} to which output will be written
	 */
	public ConsoleReporter(PrintStream out) {
		this.out = out;
	}

	/**
	 * Starts printing output to the specified {@link PrintStream}.
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
			final DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
			final String dateTime = format.format(new Date());
			out.print(dateTime);
			out.print(' ');
			for (int i = 0; i < (80 - dateTime.length() - 1); i++) {
				out.print('=');
			}
			out.println();

			for (Entry<String, Map<String, Metric>> entry : Utils.sortMetrics(Metrics.allMetrics()).entrySet()) {
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
					} else if (metric instanceof HistogramMetric) {
						printHistogram((HistogramMetric) metric);
					} else if (metric instanceof MeterMetric) {
						printMetered((MeterMetric) metric);
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

	private void printMetered(Metered meter) {
		final String unit = abbrev(meter.rateUnit());
		out.printf("             count = %d\n", meter.count());
		out.printf("         mean rate = %2.2f %s/%s\n", meter.meanRate(), meter.eventType(), unit);
		out.printf("     1-minute rate = %2.2f %s/%s\n", meter.oneMinuteRate(), meter.eventType(), unit);
		out.printf("     5-minute rate = %2.2f %s/%s\n", meter.fiveMinuteRate(), meter.eventType(), unit);
		out.printf("    15-minute rate = %2.2f %s/%s\n", meter.fifteenMinuteRate(), meter.eventType(), unit);
	}

	private void printHistogram(HistogramMetric histogram) {
		final double[] percentiles = histogram.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
		out.printf("               min = %2.2f\n", histogram.min());
		out.printf("               max = %2.2f\n", histogram.max());
		out.printf("              mean = %2.2f\n", histogram.mean());
		out.printf("            stddev = %2.2f\n", histogram.stdDev());
		out.printf("            median = %2.2f\n", percentiles[0]);
		out.printf("              75%% <= %2.2f\n", percentiles[1]);
		out.printf("              95%% <= %2.2f\n", percentiles[2]);
		out.printf("              98%% <= %2.2f\n", percentiles[3]);
		out.printf("              99%% <= %2.2f\n", percentiles[4]);
		out.printf("            99.9%% <= %2.2f\n", percentiles[5]);
	}

	private void printTimer(TimerMetric timer) {
		printMetered(timer);

		final String durationUnit = abbrev(timer.durationUnit());

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
