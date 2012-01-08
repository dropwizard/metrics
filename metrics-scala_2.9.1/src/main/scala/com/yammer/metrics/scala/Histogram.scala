package com.yammer.metrics.scala

import collection.JavaConversions._
import java.io.File

/**
 * A Scala façade class for HistogramMetric.
 *
 * @see HistogramMetric
 */
class Histogram(metric: com.yammer.metrics.core.Histogram) {

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
   * Returns a snapshot of the values in the histogram's sample.
   */
  def snapshot = metric.getSnapshot
}

