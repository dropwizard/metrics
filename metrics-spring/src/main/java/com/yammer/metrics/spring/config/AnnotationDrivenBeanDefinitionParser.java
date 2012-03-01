package com.yammer.metrics.spring.config;

import org.springframework.aop.framework.ProxyConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.spring.ExceptionMeteredAnnotationBeanPostProcessor;
import com.yammer.metrics.spring.GaugeAnnotationBeanPostProcessor;
import com.yammer.metrics.spring.HealthCheckBeanPostProcessor;
import com.yammer.metrics.spring.MeteredAnnotationBeanPostProcessor;
import com.yammer.metrics.spring.TimedAnnotationBeanPostProcessor;

import static org.springframework.beans.factory.config.BeanDefinition.*;

public class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        final Object source = parserContext.extractSource(element);

        final CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(),
                                                                                             source);
        parserContext.pushContainingComponent(compDefinition);

        String metricsBeanName = element.getAttribute("metrics-registry");
        if (!StringUtils.hasText(metricsBeanName)) {
            metricsBeanName = registerComponent(parserContext,
                                                build(Metrics.class,
                                                      source,
                                                      ROLE_APPLICATION)
                                                .setFactoryMethod("defaultRegistry"));
        }

        String healthCheckBeanName = element.getAttribute("health-check-registry");
        if (!StringUtils.hasText(healthCheckBeanName)) {
            healthCheckBeanName = registerComponent(parserContext,
                                                    build(HealthChecks.class,
                                                          source,
                                                          ROLE_APPLICATION)
                                                    .setFactoryMethod("defaultRegistry"));
        }

        String scope = element.getAttribute("scope");
        if (!StringUtils.hasText(scope)) {
            scope = null;
        }

        ProxyConfig proxyConfig = new ProxyConfig();

        if (StringUtils.hasText(element.getAttribute("expose-proxy"))) {
            proxyConfig.setExposeProxy(Boolean.valueOf(element.getAttribute("expose-proxy")));
        }

        if (StringUtils.hasText(element.getAttribute("proxy-target-class"))) {
            proxyConfig.setProxyTargetClass(Boolean.valueOf(element.getAttribute("proxy-target-class")));
        }

        registerComponent(parserContext,
                          build(ExceptionMeteredAnnotationBeanPostProcessor.class,
                                source,
                                ROLE_INFRASTRUCTURE
                          )
                          .addConstructorArgReference(metricsBeanName)
                          .addConstructorArgValue(proxyConfig)
                          .addConstructorArgValue(scope));

        registerComponent(parserContext,
                          build(MeteredAnnotationBeanPostProcessor.class,
                                source,
                                ROLE_INFRASTRUCTURE
                          )
                          .addConstructorArgReference(metricsBeanName)
                          .addConstructorArgValue(proxyConfig)
                          .addConstructorArgValue(scope));

        registerComponent(parserContext,
                          build(TimedAnnotationBeanPostProcessor.class,
                                source,
                                ROLE_INFRASTRUCTURE
                          )
                          .addConstructorArgReference(metricsBeanName)
                          .addConstructorArgValue(proxyConfig)
                          .addConstructorArgValue(scope));

        registerComponent(parserContext,
                          build(GaugeAnnotationBeanPostProcessor.class,
                                source,
                                ROLE_INFRASTRUCTURE
                          )
                          .addConstructorArgReference(metricsBeanName)
                          .addConstructorArgValue(scope));

        registerComponent(parserContext,
                          build(HealthCheckBeanPostProcessor.class,
                                source,
                                ROLE_INFRASTRUCTURE
                          )
                          .addConstructorArgReference(healthCheckBeanName));

        parserContext.popAndRegisterContainingComponent();

        return null;
    }

    private BeanDefinitionBuilder build(Class<?> klazz, Object source, int role) {
        final BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(klazz);
        beanDefBuilder.setRole(role);
        beanDefBuilder.getRawBeanDefinition().setSource(source);
        return beanDefBuilder;
    }

    private String registerComponent(ParserContext parserContext, BeanDefinitionBuilder beanDefBuilder) {
    	final BeanDefinition beanDef = beanDefBuilder.getBeanDefinition();
        final String beanName = parserContext.getReaderContext().registerWithGeneratedName(beanDef);
        parserContext.registerComponent(new BeanComponentDefinition(beanDef, beanName));
        return beanName;
    }

}
