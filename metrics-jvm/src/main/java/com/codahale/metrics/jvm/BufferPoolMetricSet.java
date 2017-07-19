package com.codahale.metrics.jvm;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

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
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();
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
