package com.yammer.metrics

import core.MeterMetric

/**
 * A Scala façade class for {@link MeterMetric}.
 *
 * @author coda
 * @see MeterMetric
 */
class Meter(metric: MeterMetric) {

  /**
   * Marks the occurance of an event.
   */
  def mark() {
    metric.mark()
  }

  /**
   * Marks the occurance of a given number of events.
   */
  def mark(count: Long) {
    metric.mark(count)
  }
}
