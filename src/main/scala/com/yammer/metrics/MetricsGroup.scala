package com.yammer.metrics

import core.GaugeMetric
import java.util.concurrent.TimeUnit

/**
 * A helper class for creating and registering metrics.
 *
 * @author coda
 */
class MetricsGroup(val builder : NameBuilder) {

  /**
   * Registers a new gauge metric.
   *
   * @param name the name of the gauge
   */
  def gauge[A](name: String)(f: => A) = {
    Metrics.newGauge(builder(name), new GaugeMetric[A] {
      def value = f
    })
  }

  /**
   * Creates a new counter metric.
   *
   * @param name the name of the counter
   */
  def counter(name: String) = new Counter(Metrics.newCounter(builder(name)))

  /**
   * Creates a new histogram metrics.
   *
   * @param name the name of the histogram
   * @param biased whether or not to use a biased sample
   */
  def histogram(name: String, biased: Boolean = false) =
    new Histogram(Metrics.newHistogram(builder(name), biased))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   * @param eventType the plural name of the type of events the meter is
   *                  measuring (e.g., {@code "requests"})
   * @param unit the time unit of the meter
   */
  def meter(name: String,
            eventType: String,
            unit: TimeUnit = TimeUnit.SECONDS) =
    new Meter(Metrics.newMeter(builder(name), eventType, unit))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   * @param durationUnit the time unit for measuring duration
   * @param rateUnit the time unit for measuring rate
   */
  def timer(name: String,
            durationUnit: TimeUnit = TimeUnit.MILLISECONDS,
            rateUnit: TimeUnit = TimeUnit.SECONDS) =
    new Timer(Metrics.newTimer(builder(name), durationUnit, rateUnit))
}
