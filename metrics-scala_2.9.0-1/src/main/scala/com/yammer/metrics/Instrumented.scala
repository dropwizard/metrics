package com.yammer.metrics

/**
 * The mixin trait for creating a class which is instrumented with metrics.
 */
trait Instrumented {
  private lazy val metricsGroup = new MetricsGroup(getClass)

  /**
   * Returns the {@link MetricsGroup} for the class.
   */
  def metrics = metricsGroup
}
