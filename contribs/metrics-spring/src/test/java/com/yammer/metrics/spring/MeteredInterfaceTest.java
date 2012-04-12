package com.yammer.metrics.spring;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yammer.metrics.core.MetricsRegistry;

public class MeteredInterfaceTest {

    ClassPathXmlApplicationContext ctx;

    @Before
    public void init() {
        this.ctx = new ClassPathXmlApplicationContext("classpath:metered-interface.xml");
    }

    @After
    public void destroy() {
        ctx.getBean(MetricsRegistry.class).shutdown();
    }

    @Test
    public void testMeteredInterface() {
        MeteredInterface mi = ctx.getBean(MeteredInterface.class);
        Assert.assertNotNull("Expected to be able to get MeteredInterface by interface and not by class.", mi);
    }

    @Test(expected=NoSuchBeanDefinitionException.class)
    public void testMeteredClass() {
        MeteredInterfaceImpl mc = ctx.getBean(MeteredInterfaceImpl.class);
        Assert.assertNull("Expected to be unable to get MeteredClass by class.", mc);
    }

    @Test
    public void testTimedMethod() {
        Assert.assertTrue(ctx.getBean(MeteredInterface.class).timedMethod());
        Assert.assertTrue(ctx.getBean(MetricsRegistry.class).allMetrics().isEmpty());
    }

    @Test
    public void testMeteredMethod() {
        Assert.assertTrue(ctx.getBean(MeteredInterface.class).meteredMethod());
        Assert.assertTrue(ctx.getBean(MetricsRegistry.class).allMetrics().isEmpty());
    }

    @Test(expected=BogusException.class)
    public void testExceptionMeteredMethod() throws Throwable {
    	try {
    		ctx.getBean(MeteredInterface.class).exceptionMeteredMethod();
    	} catch (Throwable t) {
    		Assert.assertTrue(ctx.getBean(MetricsRegistry.class).allMetrics().isEmpty());
    		throw t;
    	}
    	Assert.fail();
    }

}