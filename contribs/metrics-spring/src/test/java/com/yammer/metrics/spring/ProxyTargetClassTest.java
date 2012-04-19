package com.yammer.metrics.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProxyTargetClassTest extends Assert {

    @Test(expected = BeanCreationException.class)
    public void negativeContextLoadingTest() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:dont-proxy-target-class.xml");
        try {
            ctx.stop();
        } catch (Exception e) {
            // ignore
        }
    }

	@Test
    public void positiveContextLoadingTest() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:proxy-target-class.xml");
        assertNotNull("Expected to be able to get ProxyTargetClass by class.",ctx.getBean(ProxyTargetClass.class));
        assertNotNull("Expected to be able to get ProxyTargetClass from AutowiredCollaborator.",ctx.getBean(AutowiredCollaborator.class).getDependency());
	}

}