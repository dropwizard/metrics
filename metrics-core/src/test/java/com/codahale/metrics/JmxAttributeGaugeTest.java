package com.codahale.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class JmxAttributeGaugeTest {

    private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    private static List<ObjectName> registeredMBeans = new ArrayList<ObjectName>();

    public interface JmxTestMBean {
        Long getValue();
    }

    private static class JmxTest implements JmxTestMBean {
        @Override
        public Long getValue() {
            return Long.MAX_VALUE;
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        registerMBean(new ObjectName("JmxAttributeGaugeTest:type=test,name=test1"));
        registerMBean(new ObjectName("JmxAttributeGaugeTest:type=test,name=test2"));
    }

    @AfterClass
    public static void tearDown() {
        for (ObjectName objectName : registeredMBeans) {
            try {
                mBeanServer.unregisterMBean(objectName);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void returnsJmxAttribute() throws Exception {
        ObjectName objectName = new ObjectName("java.lang:type=ClassLoading");
        JmxAttributeGauge gauge = new JmxAttributeGauge(mBeanServer, objectName, "LoadedClassCount");

        assertThat(gauge.getValue()).isInstanceOf(Integer.class);
        assertThat((Integer) gauge.getValue()).isGreaterThan(0);
    }

    @Test
    public void returnsNullIfAttributeDoesNotExist() throws Exception {
        ObjectName objectName = new ObjectName("java.lang:type=ClassLoading");
        JmxAttributeGauge gauge = new JmxAttributeGauge(mBeanServer, objectName, "DoesNotExist");

        assertThat(gauge.getValue()).isNull();
    }

    @Test
    public void returnsNullIfMBeanNotFound() throws Exception {
        ObjectName objectName = new ObjectName("foo.bar:type=NoSuchMBean");
        JmxAttributeGauge gauge = new JmxAttributeGauge(mBeanServer, objectName, "LoadedClassCount");

        assertThat(gauge.getValue()).isNull();
    }

    @Test
    public void returnsAttributeForObjectNamePattern() throws Exception {
        ObjectName objectName = new ObjectName("JmxAttributeGaugeTest:name=test1,*");
        JmxAttributeGauge gauge = new JmxAttributeGauge(mBeanServer, objectName, "Value");

        assertThat(gauge.getValue()).isInstanceOf(Long.class);
        assertThat((Long) gauge.getValue()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    public void returnsNullIfObjectNamePatternAmbiguous() throws Exception {
        ObjectName objectName = new ObjectName("JmxAttributeGaugeTest:type=test,*");
        JmxAttributeGauge gauge = new JmxAttributeGauge(mBeanServer, objectName, "Value");

        assertThat(gauge.getValue()).isNull();
    }

    private static void registerMBean(ObjectName objectName) throws JMException {
        ObjectInstance objectInstance = mBeanServer.registerMBean(new JmxTest(), objectName);
        registeredMBeans.add(objectInstance.getObjectName());
    }

}
