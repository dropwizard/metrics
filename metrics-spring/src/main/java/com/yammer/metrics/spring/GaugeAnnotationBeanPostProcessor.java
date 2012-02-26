package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GaugeAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

    private static final AnnotationFilter filter = new AnnotationFilter(Gauge.class);

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
        final Class<?> targetClass = AopUtils.getTargetClass(bean);

        ReflectionUtils.doWithFields(targetClass, new FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                final Gauge gauge = field.getAnnotation(Gauge.class);
                final String name = gauge.name().isEmpty() ? field.getName() : gauge.name();
                metrics.newGauge(field.getDeclaringClass(),
                                 name,
                                 new GaugeField(bean, field));
            }
        }, filter);

        ReflectionUtils.doWithMethods(targetClass, new MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.getParameterTypes().length == 0) {
                    final Gauge gauge = method.getAnnotation(Gauge.class);
                    final String name = gauge.name().isEmpty() ? method.getName() : gauge.name();
                    final String group = MetricName.chooseGroup(gauge.group(), method.getDeclaringClass());
                    final String type = MetricName.chooseType(gauge.type(), method.getDeclaringClass());
                    final MetricName metricName = new MetricName(group, type, name);

                    metrics.newGauge(metricName, new GaugeMethod(bean, method));
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
