package com.yammer.metrics.core;

import java.lang.management.ManagementFactory;
import javax.management.*;

/**
 * A gauge which exposes an attribute of a JMX MBean.
 */
public class JmxGauge extends GaugeMetric<Object> {
    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();
    private ObjectName name;
    private String attribute;

    public JmxGauge(String name, String attribute) throws MalformedObjectNameException {
        this.name = new ObjectName(name);
        this.attribute = attribute;
    }

    @Override
    public Object value() {
        try {
            return SERVER.getAttribute(name, attribute);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
