package com.yammer.metrics.spring;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:proxy-target-class.xml" })
public class ProxyTargetClassTest {

	@Autowired
	ProxyTargetClass target;

	@Test
	public void loads() {
		System.out.println(target.getClass());
		Assert.assertNotNull(target);
	}

}