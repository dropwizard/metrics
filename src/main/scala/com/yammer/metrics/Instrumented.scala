package com.yammer.metrics

/**
 * The mixin trait for creating a class which is instrumented with metrics.
 */
trait Instrumented {
  protected lazy val metricsGroup = new MetricsGroup(NameBuilder(getClass))

  /**
   * Returns the {@link MetricsGroup} for the class.
   */
  def metrics = metricsGroup
}
