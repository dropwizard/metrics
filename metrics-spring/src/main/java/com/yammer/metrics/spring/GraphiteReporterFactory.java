package com.yammer.metrics.spring;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.GraphiteReporter;
import com.yammer.metrics.reporting.GraphiteReporter.DefaultSocketProvider;

public class GraphiteReporterFactory {

  public static GraphiteReporter createInstance(final MetricsRegistry registery, final String host,
      final Integer port, final TimeUnit timeUnit, final Long period, final Boolean autoStart)
      throws IOException {
    final GraphiteReporter graphiteReporter = new GraphiteReporter(registery, host, port, null);
    if (autoStart) {
      graphiteReporter.start(period, timeUnit);
    }
    return graphiteReporter;

  }

  public static GraphiteReporter createInstance(final MetricsRegistry registery, final String host,
      final Integer port, final TimeUnit timeUnit, final Long period, final Boolean autoStart,
      final String prefix) throws IOException {
    final GraphiteReporter graphiteReporter = new GraphiteReporter(registery, host, port, prefix);
    if (autoStart) {
      graphiteReporter.start(period, timeUnit);
    }
    return graphiteReporter;

  }

  public static GraphiteReporter createInstance(final MetricsRegistry registery, final String host,
      final Integer port, final TimeUnit timeUnit, final Long period, final Boolean autoStart,
      final String prefix, final MetricPredicate predicate) throws IOException {
    final GraphiteReporter graphiteReporter = new GraphiteReporter(registery, prefix, predicate,
        new DefaultSocketProvider(host, port), Clock.defaultClock());
    if (autoStart) {
      graphiteReporter.start(period, timeUnit);
    }
    return graphiteReporter;

  }

}
