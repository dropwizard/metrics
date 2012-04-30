package com.yammer.metrics.spring;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yammer.metrics.reporting.GraphiteReporter;

public class GraphiteReporterTest {

  ClassPathXmlApplicationContext ctx;

  @Before
  public void init() {
    this.ctx = new ClassPathXmlApplicationContext("classpath:graphite-reporter.xml");
  }

  @After
  public void destroy() {
    // ctx.getBean(GraphiteReporter.class).shutdown();
  }

  @Test
  public void testMeteredInterface() {
    final GraphiteReporter gr = ctx.getBean(GraphiteReporter.class);
    Assert.assertNotNull("Expected to be able to get the graphite reporter from the context.", gr);
  }

}