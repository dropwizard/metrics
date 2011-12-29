package com.yammer.metrics.scala

import java.util.concurrent.TimeUnit
import com.yammer.metrics.Metrics
import com.yammer.metrics.core.{MetricsRegistry, Gauge}

/**
 * A helper class for creating and registering metrics.
 */
class MetricsGroup(val klass: Class[_], val metricsRegistry: MetricsRegistry = Metrics.defaultRegistry()) {

  /**
   * Registers a new gauge metric.
   *
   * @param name  the name of the gauge
   * @param scope the scope of the gauge
   * @param registry the registry for the gauge
   */
  def gauge[A](name: String, scope: String = null, registry: MetricsRegistry = metricsRegistry)(f: => A) = {
    registry.newGauge(klass, name, scope, new Gauge[A] {
      def value = f
    })
  }

  /**
   * Creates a new counter metric.
   *
   * @param name  the name of the counter
   * @param scope the scope of the gauge
   * @param registry the registry for the gauge
   */
  def counter(name: String, scope: String = null, registry: MetricsRegistry = metricsRegistry) =
    new Counter(registry.newCounter(klass, name, scope))

  /**
   * Creates a new histogram metrics.
   *
   * @param name   the name of the histogram
   * @param scope  the scope of the histogram
   * @param biased whether or not to use a biased sample
   * @param registry the registry for the gauge
   */
  def histogram(name: String,
                scope: String = null,
                biased: Boolean = false,
                registry: MetricsRegistry = metricsRegistry) =
    new Histogram(registry.newHistogram(klass, name, scope, biased))

  /**
   * Creates a new meter metric.
   *
   * @param name the name of the meter
   * @param eventType the plural name of the type of events the meter is
   *                  measuring (e.g., "requests")
   * @param scope the scope of the meter
   * @param unit the time unit of the meter
   * @param registry the registry for the gauge
   */
  def meter(name: String,
            eventType: String,
            scope: String = null,
            unit: TimeUnit = TimeUnit.SECONDS,
            registry: MetricsRegistry = metricsRegistry) =
    new Meter(registry.newMeter(klass, name, scope, eventType, unit))

  /**
   * Creates a new timer metric.
   *
   * @param name the name of the timer
   * @param scope the scope of the timer
   * @param durationUnit the time unit for measuring duration
   * @param rateUnit the time unit for measuring rate
   * @param registry the registry for the gauge
   */
  def timer(name: String,
            scope: String = null,
            durationUnit: TimeUnit = TimeUnit.MILLISECONDS,
            rateUnit: TimeUnit = TimeUnit.SECONDS,
            registry: MetricsRegistry = metricsRegistry) =
    new Timer(registry.newTimer(klass, name, scope, durationUnit, rateUnit))
}

