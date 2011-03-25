package com.yammer.metrics.guice;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.TimerMetric;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A method interceptor which creates a timer for the declaring class with the
 * given name (or the method's name, if none was provided), and which times
 * the execution of the annotated method.
 */
public class TimedInterceptor implements MethodInterceptor {
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final Method method = invocation.getMethod();
		final Timed annotation = method.getAnnotation(Timed.class);
		if (annotation != null) {
			final String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
			final TimerMetric timer = Metrics.newTimer(
					method.getDeclaringClass(),
					name,
					annotation.durationUnit(),
					annotation.rateUnit()
			);
			final long startTime = System.nanoTime();
			try {
				return invocation.proceed();
			} finally {
				timer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
			}
		}
		return invocation.proceed();
	}
}
