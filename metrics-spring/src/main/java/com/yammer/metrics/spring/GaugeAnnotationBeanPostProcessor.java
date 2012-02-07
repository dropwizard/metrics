package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;

public class GaugeAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

    private static final MethodFilter filter = new AnnotationMethodFilter(Gauge.class);

    private final MetricsRegistry metrics;
    private final String scope;

    public GaugeAnnotationBeanPostProcessor(final MetricsRegistry metrics, final String scope) {
        this.metrics = metrics;
        this.scope = scope;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(AopUtils.getTargetClass(bean), new MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.getParameterTypes().length == 0) {
                    final Gauge gauge = method.getAnnotation(Gauge.class);
                    final String name = gauge.name().isEmpty() ? method.getName() : gauge.name();
                    metrics.newGauge(method.getDeclaringClass(),
                                     name,
                                     scope,
                                     new GaugeMethod(bean, method));
                } else {
                    throw new IllegalStateException("Method " + method.getName() + " is annotated with @Gauge but requires parameters.");
                }
            }
        }, filter);

        return bean;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
