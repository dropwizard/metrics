package com.yammer.metrics.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.core.MetricsRegistry;

public class MeteredAnnotationBeanPostProcessor extends AbstractProxyingBeanPostProcessor {

	private static final long serialVersionUID = 3513098435344496676L;

	private final Pointcut pointcut = new AnnotationMatchingPointcut(null, Metered.class);
	private final MetricsRegistry metrics;

	public MeteredAnnotationBeanPostProcessor(final MetricsRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}

	@Override
	public MethodInterceptor getMethodInterceptor(Class<?> targetClass) {
		return new MeteredMethodInterceptor(metrics, targetClass);
	}

}