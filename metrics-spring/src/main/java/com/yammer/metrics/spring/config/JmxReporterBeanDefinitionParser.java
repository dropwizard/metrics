package com.yammer.metrics.spring.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

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
        builder.addConstructorArgReference(element.getAttribute("metrics-registry"));
    }

}
