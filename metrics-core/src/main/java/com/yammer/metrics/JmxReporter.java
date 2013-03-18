package com.yammer.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;

/**
 * A reporter which listens for new metrics and exposes them as namespaced MBeans.
 */
public class JmxReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmxReporter.class);

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface MetricMBean {
        ObjectName objectName();
    }
    // CHECKSTYLE:ON


    private abstract static class AbstractBean implements MetricMBean {
        private final ObjectName objectName;

        AbstractBean(ObjectName objectName) {
            this.objectName = objectName;
        }

        @Override
        public ObjectName objectName() {
            return objectName;
        }
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface JmxGaugeMBean extends MetricMBean {
        Object getValue();
    }
    // CHECKSTYLE:ON

    private static class JmxGauge extends AbstractBean implements JmxGaugeMBean {
        private final Gauge<?> metric;

        private JmxGauge(Gauge<?> metric, ObjectName objectName) {
            super(objectName);
            this.metric = metric;
        }

        @Override
        public Object getValue() {
            return metric.getValue();
        }
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface JmxCounterMBean extends MetricMBean {
        long getCount();
    }
    // CHECKSTYLE:ON

    private static class JmxCounter extends AbstractBean implements JmxCounterMBean {
        private final Counter metric;

        private JmxCounter(Counter metric, ObjectName objectName) {
            super(objectName);
            this.metric = metric;
        }

        @Override
        public long getCount() {
            return metric.getCount();
        }
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface JmxHistogramMBean extends MetricMBean {
        long getCount();

        long getMin();

        long getMax();

        double getMean();

        double getStdDev();

        double get50thPercentile();

        double get75thPercentile();

        double get95thPercentile();

        double get98thPercentile();

        double get99thPercentile();

        double get999thPercentile();

        long[] values();
    }
    // CHECKSTYLE:ON

    private static class JmxHistogram implements JmxHistogramMBean {
        private final ObjectName objectName;
        private final Histogram metric;

        private JmxHistogram(Histogram metric, ObjectName objectName) {
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
            return metric.getCount();
        }

        @Override
        public long getMin() {
            return metric.getMin();
        }

        @Override
        public long getMax() {
            return metric.getMax();
        }

        @Override
        public double getMean() {
            return metric.getMean();
        }

        @Override
        public double getStdDev() {
            return metric.getStdDev();
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
        public long[] values() {
            return metric.getSnapshot().getValues();
        }
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface JmxMeterMBean extends MetricMBean {
        long getCount();

        double getMeanRate();

        double getOneMinuteRate();

        double getFiveMinuteRate();

        double getFifteenMinuteRate();
    }
    //CHECKSTYLE:ON

    private static class JmxMeter extends AbstractBean implements JmxMeterMBean {
        private final Metered metric;

        private JmxMeter(Metered metric, ObjectName objectName) {
            super(objectName);
            this.metric = metric;
        }

        @Override
        public long getCount() {
            return metric.getCount();
        }

        @Override
        public double getMeanRate() {
            return metric.getMeanRate();
        }

        @Override
        public double getOneMinuteRate() {
            return metric.getOneMinuteRate();
        }

        @Override
        public double getFiveMinuteRate() {
            return metric.getFiveMinuteRate();
        }

        @Override
        public double getFifteenMinuteRate() {
            return metric.getFifteenMinuteRate();
        }
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("UnusedDeclaration")
    public interface JmxTimerMBean extends JmxMeterMBean, JmxHistogramMBean {
    }
    // CHECKSTYLE:ON

    static class JmxTimer extends JmxMeter implements JmxTimerMBean {
        private final Timer metric;

        private JmxTimer(Timer metric, ObjectName objectName) {
            super(metric, objectName);
            this.metric = metric;
        }

        @Override
        public double get50thPercentile() {
            return metric.getSnapshot().getMedian();
        }

        @Override
        public long getMin() {
            return metric.getMin();
        }

        @Override
        public long getMax() {
            return metric.getMax();
        }

        @Override
        public double getMean() {
            return metric.getMean();
        }

        @Override
        public double getStdDev() {
            return metric.getStdDev();
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
        public long[] values() {
            return metric.getSnapshot().getValues();
        }
    }

    private static class JmxListener implements MetricRegistryListener {
        private final String name;
        private final MBeanServer mBeanServer;

        public JmxListener(MBeanServer mBeanServer, String name) {
            this.mBeanServer = mBeanServer;
            this.name = name;
        }

        @Override
        public void onGaugeAdded(String name, Gauge<?> gauge) {
            try {
                final ObjectName objectName = createName("gauges", name);
                mBeanServer.registerMBean(new JmxGauge(gauge, objectName), objectName);
            } catch (InstanceAlreadyExistsException e) {
                LOGGER.debug("Unable to register gauge", e);
            } catch (JMException e) {
                LOGGER.warn("Unable to register gauge", e);
            }
        }

        @Override
        public void onGaugeRemoved(String name) {
            try {
                mBeanServer.unregisterMBean(createName("gauges", name));
            } catch (InstanceNotFoundException e) {
                LOGGER.debug("Unable to unregister gauge", e);
            } catch (MBeanRegistrationException e) {
                LOGGER.warn("Unable to unregister gauge", e);
            }
        }

        @Override
        public void onCounterAdded(String name, Counter counter) {
            try {
                final ObjectName objectName = createName("counters", name);
                mBeanServer.registerMBean(new JmxCounter(counter, objectName), objectName);
            } catch (InstanceAlreadyExistsException e) {
                LOGGER.debug("Unable to register counter", e);
            } catch (JMException e) {
                LOGGER.warn("Unable to register counter", e);
            }
        }

        @Override
        public void onCounterRemoved(String name) {
            try {
                mBeanServer.unregisterMBean(createName("counters", name));
            } catch (InstanceNotFoundException e) {
                LOGGER.debug("Unable to unregister counter", e);
            } catch (MBeanRegistrationException e) {
                LOGGER.warn("Unable to unregister counter", e);
            }
        }

        @Override
        public void onHistogramAdded(String name, Histogram histogram) {
            try {
                final ObjectName objectName = createName("histograms", name);
                mBeanServer.registerMBean(new JmxHistogram(histogram, objectName), objectName);
            } catch (InstanceAlreadyExistsException e) {
                LOGGER.debug("Unable to register histogram", e);
            } catch (JMException e) {
                LOGGER.warn("Unable to register histogram", e);
            }
        }

        @Override
        public void onHistogramRemoved(String name) {
            try {
                mBeanServer.unregisterMBean(createName("histograms", name));
            } catch (InstanceNotFoundException e) {
                LOGGER.debug("Unable to unregister histogram", e);
            } catch (MBeanRegistrationException e) {
                LOGGER.warn("Unable to unregister histogram", e);
            }
        }

        @Override
        public void onMeterAdded(String name, Meter meter) {
            try {
                final ObjectName objectName = createName("meters", name);
                mBeanServer.registerMBean(new JmxMeter(meter, objectName), objectName);
            } catch (InstanceAlreadyExistsException e) {
                LOGGER.debug("Unable to register meter", e);
            } catch (JMException e) {
                LOGGER.warn("Unable to register meter", e);
            }
        }

        @Override
        public void onMeterRemoved(String name) {
            try {
                mBeanServer.unregisterMBean(createName("meters", name));
            } catch (InstanceNotFoundException e) {
                LOGGER.debug("Unable to unregister meter", e);
            } catch (MBeanRegistrationException e) {
                LOGGER.warn("Unable to unregister meter", e);
            }
        }

        @Override
        public void onTimerAdded(String name, Timer timer) {
            try {
                final ObjectName objectName = createName("timers", name);
                mBeanServer.registerMBean(new JmxTimer(timer, objectName), objectName);
            } catch (InstanceAlreadyExistsException e) {
                LOGGER.debug("Unable to register timer", e);
            } catch (JMException e) {
                LOGGER.warn("Unable to register timer", e);
            }
        }

        @Override
        public void onTimerRemoved(String name) {
            try {
                mBeanServer.unregisterMBean(createName("timers", name));
            } catch (InstanceNotFoundException e) {
                LOGGER.debug("Unable to unregister timer", e);
            } catch (MBeanRegistrationException e) {
                LOGGER.warn("Unable to unregister timer", e);
            }
        }

        private ObjectName createName(String type, String name) {
            try {
                return new ObjectName(this.name, "name", name);
            } catch (MalformedObjectNameException e) {
                try {
                    return new ObjectName(this.name, "name", ObjectName.quote(name));
                } catch (MalformedObjectNameException e1) {
                    LOGGER.warn("Unable to register {} {}", type, name, e1);
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    private final MetricRegistry registry;
    private final MetricRegistryListener listener;

    /**
     * Creates a new {@link JmxReporter}.
     *
     * @param mBeanServer    the platform's {@link MBeanServer}
     * @param registry       the registry containing the metrics to report
     */
    public JmxReporter(MBeanServer mBeanServer, MetricRegistry registry) {
        this.registry = registry;
        this.listener = new JmxListener(mBeanServer, registry.getName());
    }

    /**
     * Starts the reporter.
     */
    public void start() {
        registry.addListener(listener);
    }

    /**
     * Stops the reporter.
     */
    public void stop() {
        registry.removeListener(listener);
    }
}
