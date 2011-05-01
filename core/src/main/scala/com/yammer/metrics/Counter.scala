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
   * Decrements the counter by delta.
   */
  def -=(delta: Long) {
    metric.dec(delta)
  }

  /**
   * Returns the current count.
   */
  def count = metric.count

  /**
   * Resets the counter to 0.
   */
  def clear() { metric.clear() }
}
