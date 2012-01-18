package com.yammer.metrics.spring;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

import com.yammer.metrics.core.Gauge;

public class GaugeMethod extends Gauge<Object> {

	private final Object bean;
	private final Method method;

	public GaugeMethod(final Object bean, final Method method) {
		this.bean = bean;
		this.method = method;
	}

	@Override
	public Object value() {
		try {
			return method.invoke(bean);
		} catch (Exception e) {
			ReflectionUtils.rethrowRuntimeException(e);
			return null;
		}
	}

}