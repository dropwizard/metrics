package io.dropwizard.metrics.jvm;

import static io.dropwizard.metrics.MetricRegistry.name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.metrics.JmxAttributeGauge;
import io.dropwizard.metrics.Metric;
import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricSet;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of gauges for the count, usage, and capacity of the JVM's direct and mapped buffer pools.
 * <p>
 * These JMX objects are only available on Java 7 and above.
 */
public class BufferPoolMetricSet implements MetricSet {
    private static final Logger LOGGER = LoggerFactory.getLogger(BufferPoolMetricSet.class);
    private static final String[] ATTRIBUTES = { "Count", "MemoryUsed", "TotalCapacity" };
    private static final String[] NAMES = { "count", "used", "capacity" };
    private static final String[] POOLS = { "direct", "mapped" };

    private final MBeanServer mBeanServer;

    public BufferPoolMetricSet(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Override
    public Map<MetricName, Metric> getMetrics() {
        final Map<MetricName, Metric> gauges = new HashMap<MetricName, Metric>();
        for (String pool : POOLS) {
            for (int i = 0; i < ATTRIBUTES.length; i++) {
                final String attribute = ATTRIBUTES[i];
                final String name = NAMES[i];

                try {
                    final ObjectName on = new ObjectName("java.nio:type=BufferPool,name=" + pool);
                    mBeanServer.getMBeanInfo(on);
                    gauges.put(name(pool, name),
                               new JmxAttributeGauge(mBeanServer, on, attribute));
                } catch (JMException ignored) {
                    LOGGER.debug("Unable to load buffer pool MBeans, possibly running on Java 6");
                }
            }
        }
        return Collections.unmodifiableMap(gauges);
    }
}
