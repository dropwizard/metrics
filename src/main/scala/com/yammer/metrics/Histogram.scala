package com.yammer.metrics

import core.HistogramMetric

/**
 * A Scala façade class for {@link HistogramMetric}.
 *
 * @author coda
 * @see HistogramMetric
 */
class Histogram(metric: HistogramMetric) {

  /**
   * Adds the recorded value to the histogram sample.
   */
  def +=(value: Long) {
    metric.update(value)
  }

  /**
   * Adds the recorded value to the histogram sample.
   */
  def +=(value: Int) {
    metric.update(value)
  }
}
