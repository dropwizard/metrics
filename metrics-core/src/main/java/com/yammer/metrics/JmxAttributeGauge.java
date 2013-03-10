package com.yammer.metrics;

import javax.management.*;

// TODO: 3/10/13 <coda> -- write tests
// TODO: 3/10/13 <coda> -- write docs

public class JmxAttributeGauge implements Gauge<Object> {
    private final MBeanServer mBeanServer;
    private final ObjectName objectName;
    private final String attributeName;

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
