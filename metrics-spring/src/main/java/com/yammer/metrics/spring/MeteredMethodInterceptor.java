package com.yammer.metrics.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;

public class MeteredMethodInterceptor implements MethodInterceptor, MethodCallback, Ordered {

	private static final MethodFilter filter = new AnnotationMethodFilter(Metered.class);

	protected final MetricsRegistry metrics;
	protected final Class<?> targetClass;
	protected final Map<String, Meter> meters;

	public MeteredMethodInterceptor(MetricsRegistry metrics, Class<?> targetClass) {
		this.metrics = metrics;
		this.targetClass = targetClass;
		this.meters = new HashMap<String, Meter>();

		ReflectionUtils.doWithMethods(targetClass, this, filter);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		meters.get(invocation.getMethod().getName()).mark();
		return invocation.proceed();
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		Metered metered = method.getAnnotation(Metered.class);
		String methodName = method.getName();
		String meterName = metered.name().isEmpty() ? methodName : metered.name();
		Meter meter = metrics.newMeter(targetClass, meterName, metered.eventType(), metered.rateUnit());
		meters.put(methodName, meter);
	}

	@Override
	public int getOrder() {
		return 0;
	}

}