package com.yammer.metrics.spring.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

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

  private static final String HOST_ATTRIBUTE = "host";
  private static final String PORT_ATTRIBUTE = "port";
  private static final String PREFIX_ATTRIBUTE = "prefix";
  private static final String PERIOD_ATTRIBUTE = "period";
  private static final String TIMEUNIT_ATTRIBUTE = "timeUnit";
  private static final String PREDICATE_ATTRIBUTE = "predicate";

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
    builder.addConstructorArgReference(element.getAttribute("metrics-registry"));

    final String host = element.getAttribute(HOST_ATTRIBUTE);
    if (StringUtils.hasText(host)) {
      builder.addPropertyValue(HOST_ATTRIBUTE, host);
    } else {
      parserContext.getReaderContext().error("Attribute 'host' must not be empty", element);
      return;
    }

    final String port = element.getAttribute(PORT_ATTRIBUTE);
    if (StringUtils.hasText(port)) {
      builder.addPropertyValue(PORT_ATTRIBUTE, Integer.valueOf(port));
    } else {
      parserContext.getReaderContext().error("Attribute 'port' must not be empty", element);
      return;
    }

    final String prefix = element.getAttribute(PREFIX_ATTRIBUTE);
    if (StringUtils.hasText(prefix)) {
      builder.addPropertyValue(PREFIX_ATTRIBUTE, prefix);
    }
    final String period = element.getAttribute(PERIOD_ATTRIBUTE);
    if (StringUtils.hasText(period)) {
      builder.addPropertyValue(PERIOD_ATTRIBUTE, Long.valueOf(period));
    }

    final String timeUnit = element.getAttribute(TIMEUNIT_ATTRIBUTE);
    if (StringUtils.hasText(timeUnit)) {
      builder.addPropertyValue(TIMEUNIT_ATTRIBUTE, TimeUnit.valueOf(timeUnit.toUpperCase()));
    }

    final String predicate = element.getAttribute(PREDICATE_ATTRIBUTE);
    if (StringUtils.hasText(predicate)) {
      builder.addPropertyReference(PREDICATE_ATTRIBUTE, predicate);
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
