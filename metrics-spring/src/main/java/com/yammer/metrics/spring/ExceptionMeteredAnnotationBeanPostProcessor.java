package com.yammer.metrics.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.core.MetricsRegistry;

public class ExceptionMeteredAnnotationBeanPostProcessor extends AbstractProxyingBeanPostProcessor {

	private static final long serialVersionUID = -1967025297766933304L;

	private final Pointcut pointcut = new AnnotationMatchingPointcut(null, ExceptionMetered.class);
	private final MetricsRegistry metrics;

	public ExceptionMeteredAnnotationBeanPostProcessor(MetricsRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}

	@Override
	public MethodInterceptor getMethodInterceptor(Class<?> targetClass) {
		return new ExceptionMeteredMethodInterceptor(metrics, targetClass);
	}

}