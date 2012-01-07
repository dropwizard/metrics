package com.yammer.metrics.util;

import com.yammer.metrics.core.Gauge;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * A gauge which exposes an attribute of a JMX MBean.
 */
public class JmxGauge extends Gauge<Object> {
    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();
    private final ObjectName objectName;
    private final String attribute;

    /**
     * Creates a new {@link JmxGauge} for the given attribute of the given MBean.
     *
     * @param objectName    the string value of the MBean's {@link ObjectName}
     * @param attribute     the MBean attribute's name
     *
     * @throws MalformedObjectNameException if {@code objectName} is malformed
     */
    public JmxGauge(String objectName, String attribute) throws MalformedObjectNameException {
        this(new ObjectName(objectName), attribute);
    }

    /**
     * Creates a new {@link JmxGauge} for the given attribute of the given MBean.
     *
     * @param objectName    the MBean's {@link ObjectName}
     * @param attribute     the MBean attribute's name
     */
    public JmxGauge(ObjectName objectName, String attribute) {
        this.objectName = objectName;
        this.attribute = attribute;
    }

    @Override
    public Object value() {
        try {
            return SERVER.getAttribute(objectName, attribute);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
