package com.codahale.metrics;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * A {@link Gauge} implementation which queries an {@link MBeanServerConnection} for a key of a
 * {@link CompositeDataSupport} object.
 */
public class JmxCompositeDataAttributeGauge implements Gauge<Object> {
    private final MBeanServerConnection mBeanServerConn;
    private final ObjectName objectName;
    private final String attributeName;
    private final String keyName;

    /**
     * Creates a new JmxCompositeDataAttributeGauge.
     *
     * @param objectName    the name of the object.
     * @param attributeName the attribute of the {@link CompositeDataSupport} object.
     * @param keyName       the key on the {@link CompositeDataSupport} object.
     */
    public JmxCompositeDataAttributeGauge(ObjectName objectName, String attributeName, String keyName) {
        this(ManagementFactory.getPlatformMBeanServer(), objectName, attributeName, keyName);
    }

    /**
     * Creates a new JmxCompositeDataAttributeGauge.
     *
     * @param mBeanServerConn the {@link MBeanServerConnection}
     * @param objectName      the name of the object.
     * @param attributeName   the attribute of the {@link CompositeDataSupport} object.
     * @param keyName         the key on the {@link CompositeDataSupport} object.
     */
    public JmxCompositeDataAttributeGauge(MBeanServerConnection mBeanServerConn, ObjectName objectName, String attributeName, String keyName) {
        this.mBeanServerConn = mBeanServerConn;
        this.objectName = objectName;
        this.attributeName = attributeName;
        this.keyName = keyName;
    }

    @Override
    public Object getValue() {
        try {
            return ((CompositeDataSupport) mBeanServerConn.getAttribute(objectName, attributeName)).get(keyName);
        } catch (JMException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
