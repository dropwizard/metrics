package com.yammer.metrics.graphite;

import com.yammer.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class GraphiteReporter extends AbstractPollingReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteReporter.class);

    private final Graphite graphite;
    private final Clock clock;
    private final String prefix;
    private final double rateFactor;
    private final double durationFactor;


    public GraphiteReporter(MetricRegistry registry,
                            Graphite graphite,
                            Clock clock,
                            String prefix,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit) {
        super(registry, "graphite-reporter");
        this.graphite = graphite;
        this.clock = clock;
        this.prefix = prefix;
        this.rateFactor = rateUnit.toSeconds(1);
        this.durationFactor = 1.0 / durationUnit.toNanos(1);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final long timestamp = clock.getTime() / 1000;

        // oh it'd be lovely to use Java 7 here
        try {
            graphite.connect();

            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                reportGauge(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                reportCounter(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                reportHistogram(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                reportMetered(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                reportTimer(entry.getKey(), entry.getValue(), timestamp);
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to report to Graphite", graphite, e);
        } finally {
            try {
                graphite.close();
            } catch (IOException e) {
                LOGGER.debug("Error disconnecting from Graphite", graphite, e);
            }
        }
    }

    private void reportTimer(String name, Timer timer, long timestamp) throws IOException {
        graphite.write(prefix(name, "max"), format(timer.getMax() * durationFactor), timestamp);
        graphite.write(prefix(name, "mean"), format(timer.getMean() * durationFactor), timestamp);
        graphite.write(prefix(name, "min"), format(timer.getMin() * durationFactor), timestamp);
        graphite.write(prefix(name, "stddev"), format(timer.getStdDev() * durationFactor), timestamp);

        final Snapshot snapshot = timer.getSnapshot();
        graphite.write(prefix(name, "p50"), format(snapshot.getMedian() * durationFactor), timestamp);
        graphite.write(prefix(name, "p75"), format(snapshot.get75thPercentile() * durationFactor), timestamp);
        graphite.write(prefix(name, "p95"), format(snapshot.get95thPercentile() * durationFactor), timestamp);
        graphite.write(prefix(name, "p98"), format(snapshot.get98thPercentile() * durationFactor), timestamp);
        graphite.write(prefix(name, "p99"), format(snapshot.get99thPercentile() * durationFactor), timestamp);
        graphite.write(prefix(name, "p999"), format(snapshot.get999thPercentile() * durationFactor), timestamp);

        reportMetered(name, timer, timestamp);
    }

    private void reportMetered(String name, Metered meter, long timestamp) throws IOException {
        graphite.write(prefix(name, "count"), format(meter.getCount()), timestamp);
        graphite.write(prefix(name, "m1_rate"), format(meter.getOneMinuteRate() * rateFactor), timestamp);
        graphite.write(prefix(name, "m5_rate"), format(meter.getFiveMinuteRate() * rateFactor), timestamp);
        graphite.write(prefix(name, "m15_rate"), format(meter.getFifteenMinuteRate() * rateFactor), timestamp);
        graphite.write(prefix(name, "mean_rate"), format(meter.getMeanRate() * rateFactor), timestamp);
    }

    private void reportHistogram(String name, Histogram histogram, long timestamp) throws IOException {
        graphite.write(prefix(name, "count"), format(histogram.getCount()), timestamp);
        graphite.write(prefix(name, "max"), format(histogram.getMax()), timestamp);
        graphite.write(prefix(name, "mean"), format(histogram.getMean()), timestamp);
        graphite.write(prefix(name, "min"), format(histogram.getMin()), timestamp);
        graphite.write(prefix(name, "stddev"), format(histogram.getStdDev()), timestamp);

        final Snapshot snapshot = histogram.getSnapshot();
        graphite.write(prefix(name, "p50"), format(snapshot.getMedian()), timestamp);
        graphite.write(prefix(name, "p75"), format(snapshot.get75thPercentile()), timestamp);
        graphite.write(prefix(name, "p95"), format(snapshot.get95thPercentile()), timestamp);
        graphite.write(prefix(name, "p98"), format(snapshot.get98thPercentile()), timestamp);
        graphite.write(prefix(name, "p99"), format(snapshot.get99thPercentile()), timestamp);
        graphite.write(prefix(name, "p999"), format(snapshot.get999thPercentile()), timestamp);
    }

    private void reportCounter(String name, Counter counter, long timestamp) throws IOException {
        graphite.write(prefix(name, "count"), format(counter.getCount()), timestamp);
    }

    private void reportGauge(String name, Gauge gauge, long timestamp) throws IOException {
        graphite.write(prefix(name), format(gauge.getValue()), timestamp);
    }

    private String format(Object o) {
        if (o instanceof Float) {
            return format(((Float) o).doubleValue());
        } else if (o instanceof Double) {
            return format(((Double) o).doubleValue());
        } else if (o instanceof Byte) {
            return format(((Byte) o).longValue());
        } else if (o instanceof Short) {
            return format(((Short) o).longValue());
        } else if (o instanceof Integer) {
            return format(((Integer) o).longValue());
        } else if (o instanceof Long) {
            return format(((Long) o).longValue());
        }
        return String.valueOf(o);
    }

    private String prefix(String... components) {
        return MetricRegistry.name(prefix, components);
    }

    private String format(long n) {
        return Long.toString(n);
    }

    private String format(double v) {
        // the Carbon plaintext format is pretty underspecified, but it seems like it just wants
        // US-formatted digits
        return String.format(Locale.US, "%2.2f", v);
    }
}
