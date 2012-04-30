package com.yammer.metrics.spring.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.spring.GraphiteReporterFactory;

/**
 * An implementation of {@link AbstractSingleBeanDefinitionParser} which
 * initializes a graphite reporter. This class depends on the existence of the
 * <code>metrics-graphite</code> dependency.
 * 
 * <p>
 * An example bean definition:
 * 
 * <pre>
 * &lt;metrics:graphite-reporter metrics-registry="metrics" host="my.graphite.com" port="2003" timeUnit="SECONDS" period="1" /&gt;
 * </pre>
 * 
 * @author erez
 * 
 */
public class GraphiteReporterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  private static final String METRICS_REGISTRY_ATTRIBUTE = "metrics-registry";
  private static final String HOST_ATTRIBUTE = "host";
  private static final String PORT_ATTRIBUTE = "port";
  private static final String PREFIX_ATTRIBUTE = "prefix";
  private static final String PERIOD_ATTRIBUTE = "period";
  private static final String TIMEUNIT_ATTRIBUTE = "timeUnit";
  private static final String PREDICATE_ATTRIBUTE = "predicate";
  private static final String AUTOSTART_ATTRIBUTE = "autoStart";

  @Override
  protected Class<?> getBeanClass(final Element element) {
    return GraphiteReporterFactory.class;
  }

  @Override
  protected boolean shouldGenerateIdAsFallback() {
    return true;
  }

  @Override
  protected void doParse(final Element element, final ParserContext parserContext,
      final BeanDefinitionBuilder builder) {
    builder.setFactoryMethod("createInstance");
    final String registry = element.getAttribute(METRICS_REGISTRY_ATTRIBUTE);
    if (StringUtils.hasText(registry)) {
      builder.addConstructorArgReference(registry);
    } else {
      builder.addConstructorArgValue(new MetricsRegistry());
    }

    final String host = element.getAttribute(HOST_ATTRIBUTE);
    if (StringUtils.hasText(host)) {
      builder.addConstructorArgValue(host);
    } else {
      parserContext.getReaderContext().error("Attribute 'host' must not be empty", element);
      return;
    }

    final String port = element.getAttribute(PORT_ATTRIBUTE);
    if (StringUtils.hasText(port)) {
      builder.addConstructorArgValue(Integer.valueOf(port));
    } else {
      parserContext.getReaderContext().error("Attribute 'port' must not be empty", element);
      return;
    }

    final String timeUnit = element.getAttribute(TIMEUNIT_ATTRIBUTE);
    if (StringUtils.hasText(timeUnit)) {
      final TimeUnit tu = TimeUnit.valueOf(timeUnit.toUpperCase());
      if (null == tu) {
        parserContext.getReaderContext().error(
            "Attribute 'timeUnit' has invalid value: " + timeUnit
                + ", which cannot be parsed as java.util.concurrent.TimeUnit", element);
        return;
      }
      builder.addConstructorArgValue(tu);
    } else {
      parserContext.getReaderContext().error("Attribute 'timeUnit' must not be empty", element);
      return;
    }

    final String period = element.getAttribute(PERIOD_ATTRIBUTE);
    if (StringUtils.hasText(period)) {
      builder.addConstructorArgValue(Long.valueOf(period));
    } else {
      parserContext.getReaderContext().error("Attribute 'period' must not be empty", element);
      return;
    }

    final String autoStart = element.getAttribute(AUTOSTART_ATTRIBUTE);
    if (StringUtils.hasText(autoStart)) {
      builder.addConstructorArgValue(Boolean.valueOf(autoStart));
    } else {
      builder.addConstructorArgValue(true);
    }

    final String prefix = element.getAttribute(PREFIX_ATTRIBUTE);
    if (StringUtils.hasText(prefix)) {
      builder.addConstructorArgValue(prefix);
    }

    final String predicate = element.getAttribute(PREDICATE_ATTRIBUTE);
    if (StringUtils.hasText(predicate)) {
      builder.addConstructorArgReference(predicate);
    }
  }

}

/**
 * <xsd:attribute name="metrics-registry" type="xsd:string" use="required"/>
 * <xsd:attribute name="host" type="xsd:string" use="required"/> <xsd:attribute
 * name="port" type="xsd:int" use="required"/> <xsd:attribute name="prefix"
 * type="xsd:string" use="optional"/> <xsd:attribute name="priod" type="xsd:int"
 * use="optional"/> <xsd:attribute name="timeunit" type="xsd:string"
 * use="optional"/> <xsd:attribute name="predicate" type="xsd:string"
 * use="optional"/> </xsd:complexType>/
 */
