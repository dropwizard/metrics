package com.yammer.metrics.jvm;

import com.yammer.metrics.Gauge;
import com.yammer.metrics.JmxAttributeGauge;

import javax.management.*;
import java.util.*;

public class BufferPoolGaugeMap {
    private static final String[] ATTRIBUTES = { "Count", "MemoryUsed", "TotalCapacity" };
    private static final String[] NAMES = { "count", "used", "capacity" };
    private static final String[] POOLS = { "direct", "mapped" };

    private final MBeanServer mBeanServer;

    public BufferPoolGaugeMap(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    public Map<String, Gauge<?>> getGauges() {
        final Map<String, Gauge<?>> gauges = new HashMap<String, Gauge<?>>();
        for (String pool : POOLS) {
            for (int i = 0; i < ATTRIBUTES.length; i++) {
                final String attribute = ATTRIBUTES[i];
                final String name = NAMES[i];
                try {
                    final ObjectName on = new ObjectName("java.nio:type=BufferPool,name=" + pool);
                    mBeanServer.getMBeanInfo(on);
                    gauges.put("jvm.buffers." + pool + "." + name,
                               new JmxAttributeGauge(mBeanServer, on, attribute));
                } catch (JMException ignored) {

                }
            }
        }
        return Collections.unmodifiableMap(gauges);
    }
}
