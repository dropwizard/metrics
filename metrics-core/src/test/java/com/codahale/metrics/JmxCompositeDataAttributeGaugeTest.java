package com.codahale.metrics;

import org.junit.Test;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JmxCompositeDataAttributeGaugeTest {
    private final MBeanServerConnection mBeanServerConn = mock(MBeanServerConnection.class);
    private final ObjectName objectName = mock(ObjectName.class);
    private final CompositeDataSupport compositeDataSupport = mock(CompositeDataSupport.class);
    private final JmxCompositeDataAttributeGauge gauge = new JmxCompositeDataAttributeGauge(mBeanServerConn, objectName, "attr", "key");
    private final Object value = mock(Object.class);

    @Test
    public void returnsAJmxAttribute() throws Exception {
        when(compositeDataSupport.get("key")).thenReturn(value);
        when(mBeanServerConn.getAttribute(objectName, "attr")).thenReturn(compositeDataSupport);

        assertThat(gauge.getValue())
                .isEqualTo(value);
    }

    @Test
    public void returnsNullIfNoKey() throws Exception {
        when(compositeDataSupport.get("key")).thenReturn(null);
        when(mBeanServerConn.getAttribute(objectName, "attr")).thenReturn(compositeDataSupport);

        assertThat(gauge.getValue())
                .isNull();
    }

    @Test
    public void returnsNullIfThereIsAnException() throws Exception {
        when(mBeanServerConn.getAttribute(objectName, "attr")).thenThrow(new AttributeNotFoundException());

        assertThat(gauge.getValue())
                .isNull();
    }
}
