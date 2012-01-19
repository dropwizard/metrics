package com.yammer.metrics.spring;

import java.lang.reflect.Method;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.core.MetricsRegistry;

public class GaugeAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

	private static final MethodFilter filter = new AnnotationMethodFilter(Gauge.class);

	private final MetricsRegistry metrics;

	public GaugeAnnotationBeanPostProcessor(final MetricsRegistry metrics) {
		this.metrics = metrics;
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
					Gauge gauge = method.getAnnotation(Gauge.class);
					String name = gauge.name().isEmpty() ? method.getName() : gauge.name();
					metrics.newGauge(method.getDeclaringClass(), name, new GaugeMethod(bean, method));
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