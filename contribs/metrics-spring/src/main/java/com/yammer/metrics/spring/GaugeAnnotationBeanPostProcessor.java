package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log log = LogFactory.getLog(GaugeAnnotationBeanPostProcessor.class);

    private static final AnnotationFilter filter = new AnnotationFilter(Gauge.class);

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
        final Class<?> targetClass = AopUtils.getTargetClass(bean);

        ReflectionUtils.doWithFields(targetClass, new FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                final Gauge gauge = field.getAnnotation(Gauge.class);
                final String group = MetricName.chooseGroup(gauge.group(), targetClass);
                final String type = MetricName.chooseType(gauge.type(), targetClass);
                final String name = gauge.name().isEmpty() ? field.getName() : gauge.name();
                final MetricName metricName = new MetricName(group, type, name, scope);

                metrics.newGauge(metricName, new GaugeField(bean, field));

                if (log.isDebugEnabled()) {
                    log.debug("Created gauge " + metricName + " for field " + targetClass.getCanonicalName() + "." + field.getName());
                }
            }
        }, filter);

        ReflectionUtils.doWithMethods(targetClass, new MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.getParameterTypes().length > 0) {
                    throw new IllegalStateException("Method " + method.getName() + " is annotated with @Gauge but requires parameters.");
                }

                final Gauge gauge = method.getAnnotation(Gauge.class);
                final String group = MetricName.chooseGroup(gauge.group(), targetClass);
                final String type = MetricName.chooseType(gauge.type(), targetClass);
                final String name = MetricName.chooseName(gauge.name(), method);
                final MetricName metricName = new MetricName(group, type, name, scope);

                metrics.newGauge(metricName, new GaugeMethod(bean, method));

                if (log.isDebugEnabled()) {
                    log.debug("Created gauge " + metricName + " for method " + targetClass.getCanonicalName() + "." + method.getName());
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
