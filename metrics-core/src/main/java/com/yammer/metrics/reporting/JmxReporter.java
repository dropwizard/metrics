package com.yammer.metrics.reporting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheck.Result;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.HealthCheckRegistryListener;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.MetricsRegistryListener;

/**
 * A reporter which exposes application metric as JMX MBeans.
 */
public class JmxReporter extends AbstractReporter implements MetricsRegistryListener, HealthCheckRegistryListener, MetricProcessor<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmxReporter.class);
    private final Set<ObjectName> registeredBeans;
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
    
    public static interface CheckMBean extends MetricMBean {
        public boolean isHealthy();
        public String runCheck();
    }

    public static class Check extends AbstractBean implements CheckMBean {

        private final HealthCheck check;

        public Check(HealthCheck check) {
            super(check.name().getMBeanName());
            this.check = check;
        }

        @Override
        public boolean isHealthy() {
            return check.execute().isHealthy();
        }

        @Override
        public String runCheck() {
            final Result result = check.execute();
            if(result.isHealthy()) {
                return "ok";
            }
            final StringBuilder message = new StringBuilder(result.getMessage());
            final Throwable error = result.getError();
            if(error != null) {
                final StringWriter stack = new StringWriter();
                error.printStackTrace(new PrintWriter(stack, true));
                message.append('\n').append(stack.toString());
            }
            return message.toString();
        }
    }

    private static JmxReporter INSTANCE;
    private final HealthCheckRegistry healthCheckRegistry;

    /**
     * Starts the default instance of {@link JmxReporter}.
     * 
     * @param metricsRegistry the {@link MetricsRegistry} to report {@link Metric}s from
     * @param healthCheckRegistry the {@link HealthCheckRegistry} to report {@link HealthCheck}s from
     */
    public static void startDefault(MetricsRegistry metricsRegistry, HealthCheckRegistry healthCheckRegistry) {
        INSTANCE = new JmxReporter(metricsRegistry, healthCheckRegistry);
        INSTANCE.start();
    }

    /**
     * Stops the default instance of {@link JmxReporter}.
     */
    public static void shutdownDefault() {
        if(INSTANCE != null) {
            INSTANCE.shutdown();
        }
    }

    /**
     * Creates a new {@link JmxReporter} for the given registry.
     * 
     * @param metricsRegistry a {@link MetricsRegistry}
     * @param checkRegistry
     */
    public JmxReporter(MetricsRegistry metricsRegistry, HealthCheckRegistry checkRegistry) {
        super(metricsRegistry);
        this.healthCheckRegistry = checkRegistry;
        this.registeredBeans = new HashSet<ObjectName>();
        this.server = ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    public void onHealthCheckAdded(HealthCheck check) {
        if(check != null) {
            try {
                registerBean(check.name(), new Check(check));
            }
            catch(Exception e) {
                LOGGER.warn("Error processing " + check, e);
            }
        }
    }

    @Override
    public void onMetricAdded(MetricName name, Metric metric) {
        if(metric != null) {
            try {
                metric.processWith(this, name, null);
            }
            catch(Exception e) {
                LOGGER.warn("Error processing " + name, e);
            }
        }
    }

    @Override
    public void processMeter(MetricName name, Metered meter, Object unused) throws Exception {
        registerBean(name, new Meter(meter, name.getMBeanName()));
    }

    @Override
    public void processCounter(MetricName name, com.yammer.metrics.core.Counter counter, Object unused) throws Exception {
        registerBean(name, new Counter(counter, name.getMBeanName()));
    }

    @Override
    public void processHistogram(MetricName name, com.yammer.metrics.core.Histogram histogram, Object unused) throws Exception {
        registerBean(name, new Histogram(histogram, name.getMBeanName()));
    }

    @Override
    public void processTimer(MetricName name, com.yammer.metrics.core.Timer timer, Object unused) throws Exception {
        registerBean(name, new Timer(timer, name.getMBeanName()));
    }

    @Override
    public void processGauge(MetricName name, com.yammer.metrics.core.Gauge<?> gauge, Object unused) throws Exception {
        registerBean(name, new Gauge(gauge, name.getMBeanName()));
    }

    @Override
    public void onMetricRemoved(MetricName name) {
        if(registeredBeans.remove(name)) {
            unregisterBean(name.getMBeanName());
        }
    }

    @Override
    public void onHealthCheckRemoved(MetricName name) {
        if(registeredBeans.remove(name)) {
            unregisterBean(name.getMBeanName());
        }
    }

    private void unregisterBean(ObjectName objectName) {
        if(objectName != null) {
            try {
                server.unregisterMBean(objectName);
            }
            catch(Exception ignored) {}
        }
        registeredBeans.clear();
    }

    /**
     * Starts the reporter.
     */
    public final void start() {
        getMetricsRegistry().addListener(this);
        healthCheckRegistry.addListener(this);
    }

    private void registerBean(MetricName name, MetricMBean bean) throws MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
        server.registerMBean(bean, name.getMBeanName());
        registeredBeans.add(name.getMBeanName());
    }

    @Override
    public void shutdown() {
        healthCheckRegistry.removeListener(this);
        getMetricsRegistry().removeListener(this);
        for(ObjectName name : registeredBeans) {
            try {
                server.unregisterMBean(name);
            }
            catch(Exception ignored) {}
        }
    }
}
