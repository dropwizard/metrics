package com.yammer.metrics.spring.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.spring.ExceptionMeteredAnnotationBeanPostProcessor;
import com.yammer.metrics.spring.GaugeAnnotationBeanPostProcessor;
import com.yammer.metrics.spring.HealthCheckBeanPostProcessor;
import com.yammer.metrics.spring.MeteredAnnotationBeanPostProcessor;
import com.yammer.metrics.spring.TimedAnnotationBeanPostProcessor;

import static org.springframework.beans.factory.config.BeanDefinition.*;

public class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);

		CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(), source);
		parserContext.pushContainingComponent(compDefinition);

		String metricsBeanName = element.getAttribute("metrics-registry");
		if (!StringUtils.hasText(metricsBeanName)) {
			metricsBeanName = registerComponent(parserContext, source, ROLE_APPLICATION, MetricsRegistry.class, null);
		}

		String healthCheckBeanName = element.getAttribute("health-check-registry");
		if (!StringUtils.hasText(healthCheckBeanName)) {
			healthCheckBeanName = registerComponent(parserContext, source, ROLE_APPLICATION, HealthCheckRegistry.class, null);
		}

		registerComponent(parserContext, source, ROLE_INFRASTRUCTURE, ExceptionMeteredAnnotationBeanPostProcessor.class, metricsBeanName);
		registerComponent(parserContext, source, ROLE_INFRASTRUCTURE, MeteredAnnotationBeanPostProcessor.class, metricsBeanName);
		registerComponent(parserContext, source, ROLE_INFRASTRUCTURE, TimedAnnotationBeanPostProcessor.class, metricsBeanName);
		registerComponent(parserContext, source, ROLE_INFRASTRUCTURE, GaugeAnnotationBeanPostProcessor.class, metricsBeanName);
		registerComponent(parserContext, source, ROLE_INFRASTRUCTURE, HealthCheckBeanPostProcessor.class, healthCheckBeanName);

		parserContext.popAndRegisterContainingComponent();

		return null;
	}

	private String registerComponent(ParserContext parserContext, Object source, int role, Class<?> klazz, String argBeanName) {
		BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(klazz);
		beanDefBuilder.setRole(role);
		beanDefBuilder.getRawBeanDefinition().setSource(source);

		if (argBeanName != null) {
			beanDefBuilder.addConstructorArgReference(argBeanName);
		}

		return registerComponent(parserContext, beanDefBuilder.getBeanDefinition());
	}

	private String registerComponent(ParserContext parserContext, BeanDefinition beanDef) {
		String beanName = parserContext.getReaderContext().registerWithGeneratedName(beanDef);
		parserContext.registerComponent(new BeanComponentDefinition(beanDef, beanName));
		return beanName;
	}

}