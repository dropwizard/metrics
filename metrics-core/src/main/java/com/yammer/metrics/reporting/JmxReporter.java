package com.yammer.metrics.reporting;

import com.yammer.metrics.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which exposes application metric as JMX MBeans.
 */
public class JmxReporter extends AbstractReporter implements MetricsRegistryListener,
                                                             MetricProcessor<JmxReporter.Context> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmxReporter.class);

    private final Map<MetricName, ObjectName> registeredBeans;
    private final MBeanServer server;

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface MetricMBean {
        ObjectName objectName();
    }
    // CHECKSTYLE:ON


    private abstract static class AbstractBean implements MetricMBean {
        private final ObjectName objectName;

        protected AbstractBean(ObjectName objectName) {
            this.objectName = objectName;
        }

        @Override
        public ObjectName objectName() {
            return objectName;
        }
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface GaugeMBean extends MetricMBean {
        Object getValue();
    }
    // CHECKSTYLE:ON


    private static class Gauge extends AbstractBean implements GaugeMBean {
        private final com.yammer.metrics.core.Gauge<?> metric;

        private Gauge(com.yammer.metrics.core.Gauge<?> metric, ObjectName objectName) {
            super(objectName);
            this.metric = metric;
        }

        @Override
        public Object getValue() {
            return metric.value();
        }
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface CounterMBean extends MetricMBean {
        long getCount();
    }
    // CHECKSTYLE:ON


    private static class Counter extends AbstractBean implements CounterMBean {
        private final com.yammer.metrics.core.Counter metric;

        private Counter(com.yammer.metrics.core.Counter metric, ObjectName objectName) {
            super(objectName);
            this.metric = metric;
        }

        @Override
        public long getCount() {
            return metric.count();
        }
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface MeterMBean extends MetricMBean {
        long getCount();

        String getEventType();

        TimeUnit getRateUnit();

        double getMeanRate();

        double getOneMinuteRate();

        double getFiveMinuteRate();

        double getFifteenMinuteRate();
    }
    //CHECKSTYLE:ON

    private static class Meter extends AbstractBean implements MeterMBean {
        private final Metered metric;

        private Meter(Metered metric, ObjectName objectName) {
            super(objectName);
            this.metric = metric;
        }

        @Override
        public long getCount() {
            return metric.count();
        }

        @Override
        public String getEventType() {
            return metric.eventType();
        }

        @Override
        public TimeUnit getRateUnit() {
            return metric.rateUnit();
        }

        @Override
        public double getMeanRate() {
            return metric.meanRate();
        }

        @Override
        public double getOneMinuteRate() {
            return metric.oneMinuteRate();
        }

        @Override
        public double getFiveMinuteRate() {
            return metric.fiveMinuteRate();
        }

        @Override
        public double getFifteenMinuteRate() {
            return metric.fifteenMinuteRate();
        }
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface HistogramMBean extends MetricMBean {
        long getCount();

        double getMin();

        double getMax();

        double getMean();

        double getStdDev();

        double get50thPercentile();

        double get75thPercentile();

        double get95thPercentile();

        double get98thPercentile();

        double get99thPercentile();

        double get999thPercentile();

        double[] values();
    }
    // CHECKSTYLE:ON

    private static class Histogram implements HistogramMBean {
        private final ObjectName objectName;
        private final com.yammer.metrics.core.Histogram metric;

        private Histogram(com.yammer.metrics.core.Histogram metric, ObjectName objectName) {
            this.metric = metric;
            this.objectName = objectName;
        }

        @Override
        public ObjectName objectName() {
            return objectName;
        }

        @Override
        public double get50thPercentile() {
            return metric.getSnapshot().getMedian();
        }

        @Override
        public long getCount() {
            return metric.count();
        }

        @Override
        public double getMin() {
            return metric.min();
        }

        @Override
        public double getMax() {
            return metric.max();
        }

        @Override
        public double getMean() {
            return metric.mean();
        }

        @Override
        public double getStdDev() {
            return metric.stdDev();
        }

        @Override
        public double get75thPercentile() {
            return metric.getSnapshot().get75thPercentile();
        }

        @Override
        public double get95thPercentile() {
            return metric.getSnapshot().get95thPercentile();
        }

        @Override
        public double get98thPercentile() {
            return metric.getSnapshot().get98thPercentile();
        }

        @Override
        public double get99thPercentile() {
            return metric.getSnapshot().get99thPercentile();
        }

        @Override
        public double get999thPercentile() {
            return metric.getSnapshot().get999thPercentile();
        }

        @Override
        public double[] values() {
            return metric.getSnapshot().getValues();
        }
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface TimerMBean extends MeterMBean, HistogramMBean {
        TimeUnit getLatencyUnit();
    }
    // CHECKSTYLE:ON

    static class Timer extends Meter implements TimerMBean {
        private final com.yammer.metrics.core.Timer metric;

        private Timer(com.yammer.metrics.core.Timer metric, ObjectName objectName) {
            super(metric, objectName);
            this.metric = metric;
        }

        @Override
        public double get50thPercentile() {
            return metric.getSnapshot().getMedian();
        }

        @Override
        public TimeUnit getLatencyUnit() {
            return metric.durationUnit();
        }

        @Override
        public double getMin() {
            return metric.min();
        }

        @Override
        public double getMax() {
            return metric.max();
        }

        @Override
        public double getMean() {
            return metric.mean();
        }

        @Override
        public double getStdDev() {
            return metric.stdDev();
        }

        @Override
        public double get75thPercentile() {
            return metric.getSnapshot().get75thPercentile();
        }

        @Override
        public double get95thPercentile() {
            return metric.getSnapshot().get95thPercentile();
        }

        @Override
        public double get98thPercentile() {
            return metric.getSnapshot().get98thPercentile();
        }

        @Override
        public double get99thPercentile() {
            return metric.getSnapshot().get99thPercentile();
        }

        @Override
        public double get999thPercentile() {
            return metric.getSnapshot().get999thPercentile();
        }

        @Override
        public double[] values() {
            return metric.getSnapshot().getValues();
        }
    }

    private static JmxReporter INSTANCE;

    /**
     * Starts the default instance of {@link JmxReporter}.
     *
     * @param registry    the {@link MetricsRegistry} to report from
     */
    public static void startDefault(MetricsRegistry registry) {
        INSTANCE = new JmxReporter(registry);
        INSTANCE.start();
    }

    /**
     * Returns the default instance of {@link JmxReporter} if it has been started.
     *
     * @return The default instance or null if the default is not used
     */
    public static JmxReporter getDefault() {
        return INSTANCE;
    }

    /**
     * Stops the default instance of {@link JmxReporter}.
     */
    public static void shutdownDefault() {
        if (INSTANCE != null) {
            INSTANCE.shutdown();
        }
    }

    static final class Context {
        private final MetricName metricName;
        private final ObjectName objectName;

        public Context(final MetricName metricName, final ObjectName objectName) {
            this.metricName = metricName;
            this.objectName = objectName;
        }

        MetricName getMetricName() {
            return metricName;
        }

        ObjectName getObjectName() {
            return objectName;
        }
    }

    /**
     * Creates a new {@link JmxReporter} for the given registry.
     *
     * @param registry    a {@link MetricsRegistry}
     */
    public JmxReporter(MetricsRegistry registry) {
        super(registry);
        this.registeredBeans = new ConcurrentHashMap<MetricName, ObjectName>(100);
        this.server = ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    public void onMetricAdded(MetricName name, Metric metric) {
        if (metric != null) {
            try {
                metric.processWith(this, name, new Context(name, new ObjectName(name.getMBeanName())));
            } catch (Exception e) {
                LOGGER.warn("Error processing {}", name, e);
            }
        }
    }

    @Override
    public void onMetricRemoved(MetricName name) {
        final ObjectName objectName = registeredBeans.remove(name);
        if (objectName != null) {
            unregisterBean(objectName);
        }
    }

    @Override
    public void processMeter(MetricName name, Metered meter, Context context) throws Exception {
        registerBean(context.getMetricName(), new Meter(meter, context.getObjectName()),
                     context.getObjectName());
    }

    @Override
    public void processCounter(MetricName name, com.yammer.metrics.core.Counter counter, Context context) throws Exception {
        registerBean(context.getMetricName(),
                     new Counter(counter, context.getObjectName()),
                     context.getObjectName());
    }

    @Override
    public void processHistogram(MetricName name, com.yammer.metrics.core.Histogram histogram, Context context) throws Exception {
        registerBean(context.getMetricName(),
                     new Histogram(histogram, context.getObjectName()),
                     context.getObjectName());
    }

    @Override
    public void processTimer(MetricName name, com.yammer.metrics.core.Timer timer, Context context) throws Exception {
        registerBean(context.getMetricName(), new Timer(timer, context.getObjectName()),
                     context.getObjectName());
    }

    @Override
    public void processGauge(MetricName name, com.yammer.metrics.core.Gauge<?> gauge, Context context) throws Exception {
        registerBean(context.getMetricName(), new Gauge(gauge, context.getObjectName()),
                     context.getObjectName());
    }

    @Override
    public void shutdown() {
        getMetricsRegistry().removeListener(this);
        for (ObjectName name : registeredBeans.values()) {
            unregisterBean(name);
        }
        registeredBeans.clear();
    }

    /**
     * Starts the reporter.
     */
    public final void start() {
        getMetricsRegistry().addListener(this);
    }

    private void registerBean(MetricName name, MetricMBean bean, ObjectName objectName)
            throws MBeanRegistrationException, OperationsException {

        if ( server.isRegistered(objectName) ){
            server.unregisterMBean(objectName);
        }
        server.registerMBean(bean, objectName);
        registeredBeans.put(name, objectName);
    }

    private void unregisterBean(ObjectName name) {
        try {
            server.unregisterMBean(name);
        } catch (InstanceNotFoundException e) {
            // This is often thrown when the process is shutting down. An application with lots of
            // metrics will often begin unregistering metrics *after* JMX itself has cleared,
            // resulting in a huge dump of exceptions as the process is exiting.
            LOGGER.trace("Error unregistering {}", name, e);
        } catch (MBeanRegistrationException e) {
            LOGGER.debug("Error unregistering {}", name, e);
        }
    }
}
