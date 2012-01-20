package com.yammer.metrics.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;

public class HealthCheckBeanPostProcessor implements BeanPostProcessor, Ordered {

    private final HealthCheckRegistry healthChecks;

    public HealthCheckBeanPostProcessor(final HealthCheckRegistry healthChecks) {
        this.healthChecks = healthChecks;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HealthCheck) {
            healthChecks.register((HealthCheck) bean);
        }

        return bean;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
