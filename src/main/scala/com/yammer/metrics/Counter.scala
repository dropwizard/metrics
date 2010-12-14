package com.yammer.metrics

import core.CounterMetric

/**
 * A Scala façade class for {@link CounterMetric}.
 *
 * @author coda
 * @see CounterMetric
 */
class Counter(metric: CounterMetric) {

  /**
   * Increments the counter by delta.
   */
  def +=(delta: Long) {
    metric.inc(delta)
  }

  /**
   * Increments the counter by one.
   */
  def ++ {
    metric.inc()
  }

  /**
   * Decrements the counter by delta.
   */
  def -=(delta: Long) {
    metric.dec(delta)
  }

  /**
   * Decrements the counter by one.
   */
  def -- {
    metric.dec()
  }
}
