package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.util.MetricPredicate;
import com.yammer.metrics.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.core.VirtualMachineMetrics.*;

/**
 * A simple reporter which sends out application metrics to a <a href="http://ganglia.sourceforge.net/">Ganglia</a>
 * server periodically.
 * <p/>
 * NOTE: this reporter only works with Ganglia 3.1 and greater.  The message protocol for earlier
 * versions of Ganglia is different.
 * <p/>
 * This code heavily borrows from GangliaWriter in <a href="http://code.google.com/p/jmxtrans/source/browse/trunk/src/com/googlecode/jmxtrans/model/output/GangliaWriter.java">JMXTrans</a>
 * which is based on <a href="http://search-hadoop.com/c/Hadoop:/hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/metrics/ganglia/GangliaContext31.java">GangliaContext31</a>
 * from Hadoop.
 */
public class GangliaReporter extends AbstractPollingReporter implements MetricsProcessor<String> {
    private static final Logger LOG = LoggerFactory.getLogger(GangliaReporter.class);
    private static final int GANGLIA_TMAX = 60;
    private static final int GANGLIA_DMAX = 0;
    private static final String GANGLIA_INT_TYPE = "int32";
    private static final String GANGLIA_DOUBLE_TYPE = "double";
    private final MetricPredicate predicate;
    private final Locale locale = Locale.US;
    private String hostLabel;
    private String groupPrefix = "";
    private boolean compressPackageNames;
    private final GangliaMessageBuilder gangliaMessageBuilder;
    public boolean printVMMetrics = true;

    /**
     * Enables the ganglia reporter to send data for the default metrics registry to ganglia server
     * with the specified period.
     *
     * @param period      the period between successive outputs
     * @param unit        the time unit of {@code period}
     * @param gangliaHost the gangliaHost name of ganglia server (carbon-cache agent)
     * @param port        the port number on which the ganglia server is listening
     */
    public static void enable(long period, TimeUnit unit, String gangliaHost, int port) {
        enable(Metrics.defaultRegistry(), period, unit, gangliaHost, port, "");
    }

    /**
     * Enables the ganglia reporter to send data for the default metrics registry to ganglia server
     * with the specified period.
     *
     * @param period      the period between successive outputs
     * @param unit        the time unit of {@code period}
     * @param gangliaHost the gangliaHost name of ganglia server (carbon-cache agent)
     * @param port        the port number on which the ganglia server is listening
     * @param groupPrefix prefix to the ganglia group name (such as myapp_counter)
     */
    public static void enable(long period, TimeUnit unit, String gangliaHost, int port, String groupPrefix) {
        enable(Metrics.defaultRegistry(), period, unit, gangliaHost, port, groupPrefix);
    }

    /**
     * Enables the ganglia reporter to send data for the default metrics registry to ganglia server
     * with the specified period.
     *
     * @param period               the period between successive outputs
     * @param unit                 the time unit of {@code period}
     * @param gangliaHost          the gangliaHost name of ganglia server (carbon-cache agent)
     * @param port                 the port number on which the ganglia server is listening
     * @param compressPackageNames if true reporter will compress package names e.g.
     *                             com.foo.MetricName becomes c.f.MetricName
     */
    public static void enable(long period, TimeUnit unit, String gangliaHost, int port, boolean compressPackageNames) {
        enable(Metrics.defaultRegistry(),
               period,
               unit,
               gangliaHost,
               port,
               "",
               MetricPredicate.ALL,
               compressPackageNames);
    }


    /**
     * Enables the ganglia reporter to send data for the given metrics registry to ganglia server
     * with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param gangliaHost     the gangliaHost name of ganglia server (carbon-cache agent)
     * @param port            the port number on which the ganglia server is listening
     * @param groupPrefix     prefix to the ganglia group name (such as myapp_counter)
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String gangliaHost, int port, String groupPrefix) {
        enable(metricsRegistry, period, unit, gangliaHost, port, groupPrefix, MetricPredicate.ALL);
    }

    /**
     * Enables the ganglia reporter to send data to ganglia server with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param gangliaHost     the gangliaHost name of ganglia server (carbon-cache agent)
     * @param port            the port number on which the ganglia server is listening
     * @param groupPrefix     prefix to the ganglia group name (such as myapp_counter)
     * @param predicate       filters metrics to be reported
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String gangliaHost, int port, String groupPrefix, MetricPredicate predicate) {
        enable(metricsRegistry, period, unit, gangliaHost, port, groupPrefix, predicate, false);
    }

    /**
     * Enables the ganglia reporter to send data to ganglia server with the specified period.
     *
     * @param metricsRegistry      the metrics registry
     * @param period               the period between successive outputs
     * @param unit                 the time unit of {@code period}
     * @param gangliaHost          the gangliaHost name of ganglia server (carbon-cache agent)
     * @param port                 the port number on which the ganglia server is listening
     * @param groupPrefix          prefix to the ganglia group name (such as myapp_counter)
     * @param predicate            filters metrics to be reported
     * @param compressPackageNames if true reporter will compress package names e.g.
     *                             com.foo.MetricName becomes c.f.MetricName
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String gangliaHost,
                              int port, String groupPrefix, MetricPredicate predicate, boolean compressPackageNames) {
        try {
            final GangliaReporter reporter = new GangliaReporter(metricsRegistry,
                                                                 gangliaHost,
                                                                 port,
                                                                 groupPrefix,
                                                                 predicate,
                                                                 compressPackageNames);
            reporter.start(period, unit);
        } catch (Exception e) {
            LOG.error("Error creating/starting ganglia reporter:", e);
        }
    }

    /**
     * Creates a new {@link GangliaReporter}.
     *
     * @param gangliaHost is ganglia server
     * @param port        is port on which ganglia server is running
     * @throws java.io.IOException if there is an error connecting to the ganglia server
     */
    public GangliaReporter(String gangliaHost, int port) throws IOException {
        this(Metrics.defaultRegistry(), gangliaHost, port, "");
    }

    /**
     * Creates a new {@link GangliaReporter}.
     *
     * @param gangliaHost          is ganglia server
     * @param port                 is port on which ganglia server is running
     * @param compressPackageNames
     * @throws java.io.IOException if there is an error connecting to the ganglia server
     */
    public GangliaReporter(String gangliaHost, int port, boolean compressPackageNames) throws IOException {
        this(Metrics.defaultRegistry(),
             gangliaHost,
             port,
             "",
             MetricPredicate.ALL,
             compressPackageNames);
    }

    /**
     * Creates a new {@link GangliaReporter}.
     *
     * @param metricsRegistry the metrics registry
     * @param gangliaHost     is ganglia server
     * @param port            is port on which ganglia server is running
     * @param groupPrefix     prefix to the ganglia group name (such as myapp_counter)
     * @throws java.io.IOException if there is an error connecting to the ganglia server
     */
    public GangliaReporter(MetricsRegistry metricsRegistry, String gangliaHost, int port, String groupPrefix) throws IOException {
        this(metricsRegistry, gangliaHost, port, groupPrefix, MetricPredicate.ALL);
    }

    /**
     * Creates a new {@link GangliaReporter}.
     *
     * @param metricsRegistry the metrics registry
     * @param gangliaHost     is ganglia server
     * @param port            is port on which ganglia server is running
     * @param groupPrefix     prefix to the ganglia group name (such as myapp_counter)
     * @param predicate       filters metrics to be reported
     * @throws java.io.IOException if there is an error connecting to the ganglia server
     */
    public GangliaReporter(MetricsRegistry metricsRegistry, String gangliaHost, int port, String groupPrefix, MetricPredicate predicate) throws IOException {
        this(metricsRegistry, gangliaHost, port, groupPrefix, predicate, false);
    }

    /**
     * Creates a new {@link GangliaReporter}.
     *
     * @param metricsRegistry      the metrics registry
     * @param gangliaHost          is ganglia server
     * @param port                 is port on which ganglia server is running
     * @param groupPrefix          prefix to the ganglia group name (such as myapp_counter)
     * @param predicate            filters metrics to be reported
     * @param compressPackageNames if true reporter will compress package names e.g.
     *                             com.foo.MetricName becomes c.f.MetricName
     * @throws java.io.IOException if there is an error connecting to the ganglia server
     */
    public GangliaReporter(MetricsRegistry metricsRegistry, String gangliaHost, int port, String groupPrefix,
                           MetricPredicate predicate, boolean compressPackageNames) throws IOException {
        this(metricsRegistry,
             groupPrefix,
             predicate,
             compressPackageNames,
             new GangliaMessageBuilder(gangliaHost, port));
    }

    /**
     * Creates a new {@link GangliaReporter}.
     *
     * @param metricsRegistry      the metrics registry
     * @param groupPrefix          prefix to the ganglia group name (such as myapp_counter)
     * @param predicate            filters metrics to be reported
     * @param compressPackageNames if true reporter will compress package names e.g.
     *                             com.foo.MetricName becomes c.f.MetricName
     * @throws java.io.IOException if there is an error connecting to the ganglia server
     */
    public GangliaReporter(MetricsRegistry metricsRegistry, String groupPrefix,
                           MetricPredicate predicate, boolean compressPackageNames, GangliaMessageBuilder gangliaMessageBuilder) throws IOException {
        super(metricsRegistry, "ganglia-reporter");
        this.gangliaMessageBuilder = gangliaMessageBuilder;
        this.groupPrefix = groupPrefix + "_";
        this.hostLabel = getHostLabel();
        this.predicate = predicate;
        this.compressPackageNames = compressPackageNames;
    }

    @Override
    public void run() {
        if (this.printVMMetrics) {
            printVmMetrics();
        }
        printRegularMetrics();
    }

    private void printRegularMetrics() {
        for (Map.Entry<String, Map<MetricName, Metric>> entry : Utils.sortAndFilterMetrics(
                metricsRegistry.allMetrics(),
                this.predicate).entrySet()) {
            for (Map.Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
                final Metric metric = subEntry.getValue();
                if (metric != null) {
                    try {
                        metric.processWith(this, subEntry.getKey(), null);
                    } catch (Exception ignored) {
                        LOG.error("Error printing regular metrics:", ignored);
                    }
                }
            }
        }
    }

    private void sendToGanglia(String metricName, String metricType, String metricValue, String groupName, String units) {
        try {
            sendMetricData(metricType, metricName, metricValue, groupPrefix + groupName, units);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Emitting metric " + metricName + ", type " + metricType + ", value " + metricValue + " for gangliaHost: " + this
                        .gangliaMessageBuilder
                        .getHostName() + ":" + this.gangliaMessageBuilder.getPort());
            }
        } catch (IOException e) {
            LOG.error("Error sending to ganglia:", e);
        }
    }

    private void sendToGanglia(String metricName, String metricType, String metricValue, String groupName) {
        sendToGanglia(metricName, metricType, metricValue, groupName, "");
    }

    private void sendMetricData(String metricType, String metricName, String metricValue, String groupName, String units) throws IOException {

        this.gangliaMessageBuilder.newMessage()
                .addInt(128)// metric_id = metadata_msg
                .addString(this.hostLabel)// hostname
                .addString(metricName)// metric name
                .addInt(0)// spoof = True
                .addString(metricType)// metric type
                .addString(metricName)// metric name
                .addString(units)// units
                .addInt(3)// slope see gmetric.c
                .addInt(GANGLIA_TMAX)// tmax, the maximum time between metrics
                .addInt(GANGLIA_DMAX)// dmax, the maximum data value
                .addInt(1)
                .addString("GROUP")// Group attribute
                .addString(groupName)// Group value
                .send();

        this.gangliaMessageBuilder.newMessage()
                .addInt(133)// we are sending a string value
                .addString(this.hostLabel)// hostLabel
                .addString(metricName)// metric name
                .addInt(0)// spoof = True
                .addString("%s")// format field
                .addString(metricValue) // metric value
                .send();
    }

    @Override
    public void processGauge(MetricName name, GaugeMetric<?> gauge, String x) throws IOException {
        sendToGanglia(sanitizeName(name),
                      GANGLIA_INT_TYPE,
                      String.format(locale, "%s", gauge.value()),
                      "gauge");
    }

    @Override
    public void processCounter(MetricName name, CounterMetric counter, String x) throws IOException {
        sendToGanglia(sanitizeName(name),
                      GANGLIA_INT_TYPE,
                      String.format(locale, "%d", counter.count()),
                      "counter");
    }

    @Override
    public void processMeter(MetricName name, Metered meter, String x) throws IOException {
        final String sanitizedName = sanitizeName(name);
        final String units = meter.rateUnit().name();
        printLongField(sanitizedName + ".count", meter.count(), "metered", units);
        printDoubleField(sanitizedName + ".meanRate", meter.meanRate(), "metered", units);
        printDoubleField(sanitizedName + ".1MinuteRate", meter.oneMinuteRate(), "metered", units);
        printDoubleField(sanitizedName + ".5MinuteRate", meter.fiveMinuteRate(), "metered", units);
        printDoubleField(sanitizedName + ".15MinuteRate",
                         meter.fifteenMinuteRate(),
                         "metered",
                         units);
    }

    @Override
    public void processHistogram(MetricName name, HistogramMetric histogram, String x) throws IOException {
        final String sanitizedName = sanitizeName(name);
        final Double[] percentiles = histogram.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
        // TODO:  what units make sense for histograms?  should we add event type to the Histogram metric?
        printDoubleField(sanitizedName + ".min", histogram.min(), "histo");
        printDoubleField(sanitizedName + ".max", histogram.max(), "histo");
        printDoubleField(sanitizedName + ".mean", histogram.mean(), "histo");
        printDoubleField(sanitizedName + ".stddev", histogram.stdDev(), "histo");
        printDoubleField(sanitizedName + ".median", percentiles[0], "histo");
        printDoubleField(sanitizedName + ".75percentile", percentiles[1], "histo");
        printDoubleField(sanitizedName + ".95percentile", percentiles[2], "histo");
        printDoubleField(sanitizedName + ".98percentile", percentiles[3], "histo");
        printDoubleField(sanitizedName + ".99percentile", percentiles[4], "histo");
        printDoubleField(sanitizedName + ".999percentile", percentiles[5], "histo");
    }

    @Override
    public void processTimer(MetricName name, TimerMetric timer, String x) throws IOException {
        processMeter(name, timer, x);
        final String sanitizedName = sanitizeName(name);
        final Double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
        final String durationUnit = timer.durationUnit().name();
        printDoubleField(sanitizedName + ".min", timer.min(), "timer", durationUnit);
        printDoubleField(sanitizedName + ".max", timer.max(), "timer", durationUnit);
        printDoubleField(sanitizedName + ".mean", timer.mean(), "timer", durationUnit);
        printDoubleField(sanitizedName + ".stddev", timer.stdDev(), "timer", durationUnit);
        printDoubleField(sanitizedName + ".median", percentiles[0], "timer", durationUnit);
        printDoubleField(sanitizedName + ".75percentile", percentiles[1], "timer", durationUnit);
        printDoubleField(sanitizedName + ".95percentile", percentiles[2], "timer", durationUnit);
        printDoubleField(sanitizedName + ".98percentile", percentiles[3], "timer", durationUnit);
        printDoubleField(sanitizedName + ".99percentile", percentiles[4], "timer", durationUnit);
        printDoubleField(sanitizedName + ".999percentile", percentiles[5], "timer", durationUnit);
    }

    private void printDoubleField(String name, double value, String groupName, String units) {
        sendToGanglia(name,
                      GANGLIA_DOUBLE_TYPE,
                      String.format(locale, "%2.2f", value),
                      groupName,
                      units);
    }

    private void printDoubleField(String name, double value, String groupName) {
        printDoubleField(name, value, groupName, "");
    }

    private void printLongField(String name, long value, String groupName) {
        printLongField(name, value, groupName, "");
    }

    private void printLongField(String name, long value, String groupName, String units) {
        // TODO:  ganglia does not support int64, what should we do here?
        sendToGanglia(name, GANGLIA_INT_TYPE, String.format(locale, "%d", value), groupName, units);
    }

    private void printVmMetrics() {
        printDoubleField("jvm.memory.heap_usage", heapUsage(), "jvm");
        printDoubleField("jvm.memory.non_heap_usage", nonHeapUsage(), "jvm");
        for (Map.Entry<String, Double> pool : memoryPoolUsage().entrySet()) {
            printDoubleField("jvm.memory.memory_pool_usages." + pool.getKey(),
                             pool.getValue(),
                             "jvm");
        }

        printDoubleField("jvm.daemon_thread_count", daemonThreadCount(), "jvm");
        printDoubleField("jvm.thread_count", threadCount(), "jvm");
        printDoubleField("jvm.uptime", uptime(), "jvm");
        printDoubleField("jvm.fd_usage", fileDescriptorUsage(), "jvm");

        for (Map.Entry<Thread.State, Double> entry : threadStatePercentages().entrySet()) {
            printDoubleField("jvm.thread-states." + entry.getKey().toString().toLowerCase(),
                             entry.getValue(),
                             "jvm");
        }

        for (Map.Entry<String, VirtualMachineMetrics.GarbageCollector> entry : garbageCollectors().entrySet()) {
            printLongField("jvm.gc." + entry.getKey() + ".time",
                           entry.getValue().getTime(TimeUnit.MILLISECONDS),
                           "jvm");
            printLongField("jvm.gc." + entry.getKey() + ".runs", entry.getValue().getRuns(), "jvm");
        }
    }

    String getHostLabel() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress() + ":" + addr.getHostName();
        } catch (UnknownHostException e) {
            LOG.error("Unable to get local gangliaHost name: ", e);
            return "unknown";
        }
    }

    protected String sanitizeName(MetricName name) {
        if (name == null) {
            return "";
        }
        final String qualifiedTypeName = name.getGroup() + "." + name.getType() + "." + name.getName();
        final String metricName = name.hasScope() ? qualifiedTypeName + '.' + name.getScope() : qualifiedTypeName;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < metricName.length(); i++) {
            char p = metricName.charAt(i);
            if (!(p >= 'A' && p <= 'Z')
                    && !(p >= 'a' && p <= 'z')
                    && !(p >= '0' && p <= '9')
                    && (p != '_')
                    && (p != '-')
                    && (p != '.')
                    && (p != '\0')) {
                sb.append('_');
            } else {
                sb.append(p);
            }
        }
        return compressPackageName(sb.toString());
    }

    private String compressPackageName(String name) {
        if (compressPackageNames && name.indexOf(".") > 0) {
            String[] nameParts = name.split("\\.");
            StringBuilder sb = new StringBuilder();
            int numParts = nameParts.length;
            int count = 0;
            for (String namePart : nameParts) {
                if (++count < numParts - 1) {
                    sb.append(namePart.charAt(0));
                    sb.append(".");
                } else {
                    sb.append(namePart);
                    if (count == numParts - 1) {
                        sb.append(".");
                    }
                }
            }
            name = sb.toString();
        }
        return name;
    }
}
