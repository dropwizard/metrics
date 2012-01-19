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

import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class TimedMethodInterceptor implements MethodInterceptor, MethodCallback, Ordered {

	private static final MethodFilter filter = new AnnotationMethodFilter(Timed.class);

	private final MetricsRegistry metrics;
	private final Class<?> targetClass;
	private final Map<String, Timer> timers;

	public TimedMethodInterceptor(final MetricsRegistry metrics, final Class<?> targetClass) {
		this.metrics = metrics;
		this.targetClass = targetClass;
		this.timers = new HashMap<String, Timer>();

		ReflectionUtils.doWithMethods(targetClass, this, filter);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		TimerContext tc = timers.get(invocation.getMethod().getName()).time();
		try {
			return invocation.proceed();
		} finally {
			tc.stop();
		}
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		Timed timed = method.getAnnotation(Timed.class);
		String methodName = method.getName();
		String timerName = timed.name().isEmpty() ? methodName: timed.name();
		Timer timer = metrics.newTimer(targetClass, timerName, timed.durationUnit(), timed.rateUnit());
		timers.put(methodName, timer);
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

}
