package com.yammer.metrics.spring.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class MetricsNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("annotation-driven",
                                     new AnnotationDrivenBeanDefinitionParser());
        registerBeanDefinitionParser("metrics-registry", new MetricsRegistryBeanDefinitionParser());
        registerBeanDefinitionParser("health-check-registry",
                                     new HealthCheckRegistryBeanDefinitionParser());
        registerBeanDefinitionParser("jmx-reporter", new JmxReporterBeanDefinitionParser());
    }

}
