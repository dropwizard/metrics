package com.yammer.metrics.spring.config;

import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

import com.yammer.metrics.core.HealthCheckRegistry;

public class HealthCheckRegistryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return HealthCheckRegistry.class;
    }

}
