package com.yammer.metrics.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.MetricsRegistry;

public class TimedAnnotationBeanPostProcessor extends AbstractProxyingBeanPostProcessor {

    private static final long serialVersionUID = -1589475386869891203L;

    private final Pointcut pointcut = new AnnotationMatchingPointcut(null, Timed.class);
    private final MetricsRegistry metrics;

    public TimedAnnotationBeanPostProcessor(final MetricsRegistry metrics) {
        this.metrics = metrics;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public MethodInterceptor getMethodInterceptor(Class<?> targetClass) {
        return new TimedMethodInterceptor(metrics, targetClass);
    }

}
