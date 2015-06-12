package io.dropwizard.metrics;

import org.junit.Test;

import io.dropwizard.metrics.JmxAttributeGauge;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JmxAttributeGaugeTest {
    private final MBeanServer mBeanServer = mock(MBeanServer.class);
    private final ObjectName objectName = mock(ObjectName.class);
    private final JmxAttributeGauge gauge = new JmxAttributeGauge(mBeanServer, objectName, "attr");
    private final Object value = mock(Object.class);

    @Test
    public void returnsAJmxAttribute() throws Exception {
        when(mBeanServer.getAttribute(objectName, "attr")).thenReturn(value);

        assertThat(gauge.getValue())
                .isEqualTo(value);
    }

    @Test
    public void returnsNullIfThereIsAnException() throws Exception {
        when(mBeanServer.getAttribute(objectName, "attr")).thenThrow(new AttributeNotFoundException());

        assertThat(gauge.getValue())
                .isNull();
    }
}
