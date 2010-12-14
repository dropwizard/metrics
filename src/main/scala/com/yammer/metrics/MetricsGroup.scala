package com.yammer.metrics

import core.{MetricsFactory, GaugeMetric}
import java.util.concurrent.TimeUnit

/**
 * A helper class for creating and registering metrics.
 *
 * @author coda
 */
class MetricsGroup(val klass: Class[_]) {

  /**
   * Registers a new gauge metric.
   *
   * @param name the name of the gauge
   */
  def gauge[A](name: String)(f: => A) {
    MetricsFactory.newGauge(klass, name, new GaugeMetric[A] {
      def value = f
    })
  }

  /**
   * Creates a new counter metric.
   *
   * @param name the name of the counter
   */
  def counter(name: String) = new Counter(MetricsFactory.newCounter(klass, name))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   * @param eventType the plural name of the type of events the meter is
   *                  measuring (e.g., {@code "requests"})
   * @param unit the time unit of the meter
   */
  def meter(name: String, eventType: String, unit: TimeUnit) =
    new Meter(MetricsFactory.newMeter(klass, name, eventType, unit))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   * @param latencyUnit the time unit for measuring latency
   * @param rateUnit the time unit for measuring rate
   */
  def timer(name: String, latencyUnit: TimeUnit, rateUnit: TimeUnit) =
    new Timer(MetricsFactory.newTimer(klass, name, latencyUnit, rateUnit))
}
