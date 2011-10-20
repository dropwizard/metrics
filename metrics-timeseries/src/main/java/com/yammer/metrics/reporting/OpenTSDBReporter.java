package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.util.MetricPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple reporter which sends out application metrics to a
 * <a href="http://opentsdb.net/overview.html">OpenTSDB</a> server periodically.
 */
public class OpenTSDBReporter extends AbstractTimeSeriesReporter {
    private static final Logger LOG = LoggerFactory.getLogger(OpenTSDBReporter.class);
    private final String tags;

    /**
     * Enables the graphite reporter to send data for the default metrics registry
     * to graphite server with the specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     * @param host   the host name of graphite server (carbon-cache agent)
     * @param port   the port number on which the graphite server is listening
     */
    public static void enable(long period, TimeUnit unit, String host, int port) {
        enable(Metrics.defaultRegistry(), period, unit, host, port);
    }

    /**
     * Enables the graphite reporter to send data for the given metrics registry
     * to graphite server with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param host            the host name of graphite server (carbon-cache agent)
     * @param port            the port number on which the graphite server is listening
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port) {
        enable(metricsRegistry, period, unit, host, port, new ArrayList<String>());
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the
     * specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     * @param host   the host name of graphite server (carbon-cache agent)
     * @param port   the port number on which the graphite server is listening
     * @param tags   the list of key=value tags to associate with each TSD point
     */
    public static void enable(long period, TimeUnit unit, String host, int port, List<String> tags) {
        enable(Metrics.defaultRegistry(), period, unit, host, port, tags);
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the
     * specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param host            the host name of graphite server (carbon-cache agent)
     * @param port            the port number on which the graphite server is listening
     * @param tags            the list of key=value tags to associate with each TSD point
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port, List<String> tags) {
        enable(metricsRegistry, period, unit, host, port, tags, MetricPredicate.ALL);
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the
     * specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param host            the host name of graphite server (carbon-cache agent)
     * @param port            the port number on which the graphite server is listening
     * @param tags            the list of key=value tags to associate with each TSD point
     * @param predicate       filters metrics to be reported
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port, List<String> tags, MetricPredicate predicate) {
        try {
            final OpenTSDBReporter reporter = new OpenTSDBReporter(metricsRegistry, host, port, tags, predicate);
            reporter.start(period, unit);
        } catch (Exception e) {
            LOG.error("Error creating/starting OpenTSDBReporter reporter:", e);
        }
    }

    /**
     * Creates a new {@link OpenTSDBReporter}.
     *
     * @param host is graphite server
     * @param port is port on which graphite server is running
     *
     * @throws IOException if there is an error connecting to the OpenTSDBReporter server
     */
    public OpenTSDBReporter(String host, int port) throws IOException {
        this(Metrics.defaultRegistry(), host, port, new ArrayList<String>());
    }

    /**
     * Creates a new {@link OpenTSDBReporter}.
     *
     * @param host is graphite server
     * @param port is port on which graphite server is running
     * @param tags the list of key=value tags to associate with each TSD point
     *
     * @throws IOException if there is an error connecting to the OpenTSDBReporter server
     */
    public OpenTSDBReporter(String host, int port, List<String> tags) throws IOException {
        this(Metrics.defaultRegistry(), host, port, tags);
    }

    /**
     * Creates a new {@link OpenTSDBReporter}.
     *
     * @param metricsRegistry the metrics registry
     * @param host            is graphite server
     * @param port            is port on which graphite server is running
     * @param tags            the list of key=value tags to associate with each TSD point
     *
     * @throws IOException if there is an error connecting to the OpenTSDBReporter server
     */
    public OpenTSDBReporter(MetricsRegistry metricsRegistry, String host, int port, List<String> tags) throws IOException {
        this(metricsRegistry, host, port, tags, MetricPredicate.ALL);
    }

    protected OpenTSDBReporter(MetricsRegistry metricsRegistry, String host, int port, List<String> tags, MetricPredicate predicate) {
        super(metricsRegistry, "opentsdb-reporter", host, port, predicate);

        final StringBuilder builder = new StringBuilder(tags.size());
        for (String tagAndValue : tags) {
            builder.append(tagAndValue);
        }

        this.tags = builder.toString();
    }

    @Override
    protected String formatDoubleField(String sanitizedName, long epoch, double value) {
        return String.format(locale, "put %s %d %2.2f %s\n", sanitizedName, epoch, value, tags);
    }

    @Override
    protected String formatLongField(String sanitizedName, long epoch, long value) {
        return String.format(locale, "put %s %d %d %s\n", sanitizedName, epoch, value, tags);
    }

    @Override
    protected String formatGaugeField(String sanitizedName, long epoch, GaugeMetric<?> gauge) {
        return String.format(locale, "put %s %d %s %s\n", sanitizedName, epoch, gauge.value(), tags);
    }
}
