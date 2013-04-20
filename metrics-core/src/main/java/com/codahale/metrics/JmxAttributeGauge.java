package com.codahale.metrics;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * A {@link Gauge} implementation which queries a {@link MBeanServer} for an attribute of an object.
 */
public class JmxAttributeGauge implements Gauge<Object> {
    private final MBeanServer mBeanServer;
    private final ObjectName objectName;
    private final String attributeName;

    /**
     * Creates a new JmxAttributeGauge.
     *
     * @param objectName    the name of the object
     * @param attributeName the name of the object's attribute
     */
    public JmxAttributeGauge(ObjectName objectName, String attributeName) {
        this(ManagementFactory.getPlatformMBeanServer(), objectName, attributeName);
    }

    /**
     * Creates a new JmxAttributeGauge.
     *
     * @param mBeanServer      the {@link MBeanServer}
     * @param objectName       the name of the object
     * @param attributeName    the name of the object's attribute
     */
    public JmxAttributeGauge(MBeanServer mBeanServer, ObjectName objectName, String attributeName) {
        this.mBeanServer = mBeanServer;
        this.objectName = objectName;
        this.attributeName = attributeName;
    }

    @Override
    public Object getValue() {
        try {
            return mBeanServer.getAttribute(objectName, attributeName);
        } catch (JMException e) {
            return null;
        }
    }
}
