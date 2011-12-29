package com.yammer.metrics.scala

/**
 * A Scala façade class for Counter.
 */
class Counter(metric: com.yammer.metrics.core.Counter) {

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
