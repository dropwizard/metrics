package com.yammer.metrics.spring;

import java.util.concurrent.TimeUnit;

import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.GraphiteReporter;

public class GraphiteReporterFactory  {

  private String prefix;
  private int port;
  private String host;
  private TimeUnit timeUnit;
  private long period;
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

  public void setPredicate(MetricPredicate predicate) {
	this.predicate = predicate;
}
  
  public void createInstance()  {
    GraphiteReporter.enable(registery, period, timeUnit, host, port, prefix, predicate);
  }

}
