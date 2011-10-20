package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPollingReporter extends AbstractReporter implements Runnable {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private long pollingTime;
  private TimeUnit pollingTimeUnit;

  protected AbstractPollingReporter(MetricsRegistry metricsRegistry, String name) {
    super(metricsRegistry, name);
  }

  public final void start(long pollingTime, TimeUnit pollingTimeUnit) {
    executor.scheduleWithFixedDelay(this, pollingTime, pollingTime, pollingTimeUnit);
  }

  public void shutdown(long waitTime, TimeUnit waitTimeMillis) throws InterruptedException {
    executor.shutdown();
    executor.awaitTermination(waitTime, waitTimeMillis);
  }

  public void shutdown() {
    executor.shutdown();
  }
}
