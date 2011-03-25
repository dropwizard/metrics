package com.yammer.metrics.guice;

import java.lang.reflect.Method;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MeterMetric;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A method interceptor which creates a meter for the declaring class with the
 * given name (or the method's name, if none was provided), and which measures
 * the rate at which the annotated method is invoked.
 */
public class MeteredInterceptor implements MethodInterceptor {
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final Method method = invocation.getMethod();
		final Metered annotation = method.getAnnotation(Metered.class);
		if (annotation != null) {
			final String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
			final MeterMetric meter = Metrics.newMeter(method.getDeclaringClass(),
					name, annotation.eventType(), annotation.rateUnit());
			meter.mark();
		}
		return invocation.proceed();
	}
}
