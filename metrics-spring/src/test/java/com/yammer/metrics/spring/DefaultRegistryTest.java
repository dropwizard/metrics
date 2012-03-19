package com.yammer.metrics.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;

public class DefaultRegistryTest {

    @Test
    public void testDefaultRegistries() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:default-registries.xml");
        Assert.assertSame("Should be default MetricsRegistry.", Metrics.defaultRegistry(), ctx.getBean(MetricsRegistry.class));
        Assert.assertSame("Should be default HealthCheckRegistry.", HealthChecks.defaultRegistry(), ctx.getBean(HealthCheckRegistry.class));
    }

    @Test
    public void testSuppliedRegistries() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:supplied-registries.xml");
        Assert.assertNotSame("Should have provided MetricsRegistry.", Metrics.defaultRegistry(), ctx.getBean(MetricsRegistry.class));
        Assert.assertNotSame("Should have provided HealthCheckRegistry.", HealthChecks.defaultRegistry(), ctx.getBean(HealthCheckRegistry.class));
    }

}