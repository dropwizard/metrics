package com.yammer.metrics.scala

import com.yammer.metrics.Metrics

/**
 * The mixin trait for creating a class which is instrumented with metrics.
 */
trait Instrumented {
  private lazy val metricsGroup = new MetricsGroup(getClass, metricsRegistry)

  /**
   * Returns the MetricsGroup for the class.
   */
  def metrics = metricsGroup

  /**
   * Returns the MetricsRegistry for the class.
   */
  def metricsRegistry = Metrics.defaultRegistry()
}

