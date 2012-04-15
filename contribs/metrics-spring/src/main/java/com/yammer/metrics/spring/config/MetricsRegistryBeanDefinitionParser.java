package com.yammer.metrics.spring.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.yammer.metrics.core.MetricsRegistry;

public class MetricsRegistryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return MetricsRegistry.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String clock = element.getAttribute("clock");
        if (StringUtils.hasText(clock)) {
            builder.addConstructorArgReference(clock);
        }
    }

}
