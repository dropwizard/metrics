package com.yammer.metrics

import core.GaugeMetric
import java.util.concurrent.TimeUnit

/**
 * A helper class for creating and registering metrics.
 */
class MetricsGroup(val klass: Class[_]) {

  /**
   * Registers a new gauge metric.
   *
   * @param name  the name of the gauge
   * @param scope the scope of the gauge
   */
  def gauge[A](name: String, scope: String = null)(f: => A) = {
    Metrics.newGauge(klass, name, scope, new GaugeMetric[A] {
      def value = f
    })
  }

  /**
   * Creates a new counter metric.
   *
   * @param name  the name of the counter
   * @param scope the scope of the gauge
   */
  def counter(name: String, scope: String = null) =
    new Counter(Metrics.newCounter(klass, name, scope))

  /**
   * Creates a new histogram metrics.
   *
   * @param name   the name of the histogram
   * @param scope  the scope of the histogram
   * @param biased whether or not to use a biased sample
   */
  def histogram(name: String,
                scope: String = null,
                biased: Boolean = false) =
    new Histogram(Metrics.newHistogram(klass, name, scope, biased))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   * @param eventType the plural name of the type of events the meter is
   *                  measuring (e.g., {@code "requests"})
   * @param scope the scope of the meter
   * @param unit the time unit of the meter
   */
  def meter(name: String,
            eventType: String,
            scope: String = null,
            unit: TimeUnit = TimeUnit.SECONDS) =
    new Meter(Metrics.newMeter(klass, name, scope, eventType, unit))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   * @param scope the scope of the timer
   * @param durationUnit the time unit for measuring duration
   * @param rateUnit the time unit for measuring rate
   */
  def timer(name: String,
            scope: String = null,
            durationUnit: TimeUnit = TimeUnit.MILLISECONDS,
            rateUnit: TimeUnit = TimeUnit.SECONDS) =
    new Timer(Metrics.newTimer(klass, name, scope, durationUnit, rateUnit))
}
