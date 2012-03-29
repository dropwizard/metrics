package com.yammer.metrics.spring;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.StringUtils;

import com.yammer.metrics.core.MetricName;

/**
 * A configurable implementation of the
 * {@link MethodInvocationMetricNameFactory} which accepts required parameters
 * in the constructor and optional parameters as setters which can be used
 * easily within a spring context definition. Based on which optional parameters
 * are provided the appropriate {@link MetricName} constructor will be invoked
 * 
 * @author erez
 * 
 */
public class ConfigurableMethodInvocationMetricNameFactory implements
		MethodInvocationMetricNameFactory {

	private String group;
	private String type;
	private String scope;
	private String mbeanName;

	public ConfigurableMethodInvocationMetricNameFactory(String group,
			String type) {
		this.group = group;
		this.type = type;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setMbeanName(String mbeanName) {
		this.mbeanName = mbeanName;
	}

	@Override
	public MetricName createMetricName(MethodInvocation invocation) {
		if (StringUtils.hasText(scope) && StringUtils.hasText(mbeanName)) {
			return new MetricName(group, type,
					invocation.getMethod().getName(), scope, mbeanName);
		}

		if (StringUtils.hasText(scope)) {
			return new MetricName(group, type,
					invocation.getMethod().getName(), scope);
		}
		return new MetricName(group, type, invocation.getMethod().getName());
	}

}
