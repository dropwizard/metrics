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
  def count = metric.getCount

  /**
   * Clears all recorded values.
   */
  def clear() { metric.clear() }

  /**
   * Returns the largest recorded value.
   */
  def max = metric.getMax

  /**
   * Returns the smallest recorded value.
   */
  def min = metric.getMin

  /**
   * Returns the arithmetic mean of all recorded values.
   */
  def mean = metric.getMean

  /**
   * Returns the standard deviation of all recorded values.
   */
  def stdDev = metric.getStdDev

  /**
   * Returns a snapshot of the values in the histogram's sample.
   */
  def snapshot = metric.getSnapshot
}

