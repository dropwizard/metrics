package com.yammer.metrics.spring.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.spring.JmxReporterFactory;

public class JmxReporterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return JmxReporterFactory.class;
    }

    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        builder.setFactoryMethod("createInstance");
        String registry = element.getAttribute("metrics-registry");
        if (StringUtils.hasText(registry)) {
        	builder.addConstructorArgReference(registry);
        } else {
        	builder.addConstructorArgValue(Metrics.defaultRegistry());
        }
    }

}
