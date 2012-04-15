package com.yammer.metrics.spring;

import org.aopalliance.intercept.MethodInvocation;

import com.yammer.metrics.core.MetricName;

/**
 * A factory for a {@link MetricName} name object based on a
 * {@link MethodInvocation}, since in AOP based interceptor providing the
 * {@link MethodInvocation#getClass()} usually returns a spring class, classes
 * wishing to use reflection can provide a configurable interceptor which
 * customizes the group, type and name creation
 * 
 * @author erez
 * 
 */

public interface MethodInvocationMetricNameFactory {
  /**
   * Create a metric name based on a {@link MethodInvocation}
   * 
   * @param invocation
   *          the instance of the invocation
   * @return
   */
  MetricName createMetricName(MethodInvocation invocation);

}
