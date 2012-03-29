package com.yammer.metrics.spring;

import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;

import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.GraphiteReporter;

public class GraphiteReporterFactory {

  private String prefix;
  private Integer port;
  private String host;
  private TimeUnit timeUnit;
  private Long period;
  private MetricsRegistry registery;
  private MetricPredicate predicate;

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  public void setHost(final String host) {
    this.host = host;
  }

  public void setTimeUnit(final TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
  }

  public void setPeriod(final long period) {
    this.period = period;
  }

  public void setRegistery(final MetricsRegistry registery) {
    this.registery = registery;
  }

  public void setPredicate(final MetricPredicate predicate) {
    this.predicate = predicate;
  }

  public void createInstance() {
    if (null != predicate && StringUtils.hasText(prefix)) {
      GraphiteReporter.enable(registery, period, timeUnit, host, port, prefix, predicate);
    } else if (StringUtils.hasText(prefix)) {
      GraphiteReporter.enable(registery, period, timeUnit, host, port, prefix);
    } else {
      GraphiteReporter.enable(registery, period, timeUnit, host, port);
    }
  }

}
