package com.yammer.metrics.spring;

import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.Assert;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class MetricsTimerMethodInterceptor implements MethodInterceptor {
	private final MetricsRegistry metricsRegistry;

	private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
	private TimeUnit rateUnit = TimeUnit.MICROSECONDS;
	private MethodInvocationMetricNameFactory metricNameFactory;

	public MetricsTimerMethodInterceptor(final MetricsRegistry metricsRegistry,
			MethodInvocationMetricNameFactory metricNameFactory) {
		Assert.notNull(metricsRegistry, "metricsRegistry cannot be null");
		Assert.notNull(metricNameFactory, "metricNameFactory cannot be null");
		this.metricsRegistry = metricsRegistry;
		this.metricNameFactory = metricNameFactory;
	}

	public void setDurationUnit(final TimeUnit durationUnit) {
		this.durationUnit = durationUnit;
	}

	public void setRateUnit(final TimeUnit rateUnit) {
		this.rateUnit = rateUnit;
	}

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		final Timer timer = metricsRegistry.newTimer(
				metricNameFactory.createMetricName(invocation), durationUnit,
				rateUnit);
		final TimerContext context = timer.time();
		try {
			return invocation.proceed();
		} finally {
			context.stop();
		}
	}

}
