package com.yammer.metrics

import collection.JavaConversions._
import core.HistogramMetric
import java.io.File

/**
 * A Scala façade class for {@link HistogramMetric}.
 *
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

  /**
   * Returns the number of values recorded.
   */
  def count = metric.count

  /**
   * Clears all recorded values.
   */
  def clear() { metric.clear() }

  /**
   * Returns the largest recorded value.
   */
  def max = metric.max

  /**
   * Returns the smallest recorded value.
   */
  def min = metric.min

  /**
   * Returns the arithmetic mean of all recorded values.
   */
  def mean = metric.mean

  /**
   * Returns the standard deviation of all recorded values.
   */
  def stdDev = metric.stdDev

  /**
   * Returns a sequence of all values in the histogram's sample.
   */
  def values = metric.values.toSeq

  /**
   * Returns an array of values at the given percentiles.
   */
  def percentiles(percentiles: Double*) = metric.percentiles(percentiles:_*)

  /**
   * Dumps the recoded values in the timer's sample to the given file.
   */
  def dump(output: File) {
    metric.dump(output)
  }
}
