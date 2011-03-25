package com.yammer.metrics

import collection.JavaConversions._
import core.TimerMetric
import java.util.concurrent.TimeUnit

/**
 * A Scala façade class for {@link TimerMetric}.
 *
 * @author coda
 * @see TimerMetric
 */
class Timer(metric: TimerMetric) {
  /**
   * Runs f, recording its duration, and returns the result of f.
   */
  def time[A](f: => A): A = {
    val startTime = System.nanoTime
    try {
      f
    } finally {
      metric.update(System.nanoTime - startTime, TimeUnit.NANOSECONDS)
    }
  }

  /**
   * Adds a recorded duration.
   */
  def update(duration: Long, unit: TimeUnit) {
    metric.update(duration, unit)
  }

  /**
   * Returns the number of durations recorded.
   */
  def count = metric.count

  /**
   * Clears all recorded durations.
   */
  def clear() {metric.clear()}

  /**
   * Returns the longest recorded duration.
   */
  def max = metric.max

  /**
   * Returns the shortest recorded duration.
   */
  def min = metric.min

  /**
   * Returns the arithmetic mean of all recorded durations.
   */
  def mean = metric.mean

  /**
   * Returns the standard deviation of all recorded durations.
   */
  def stdDev = metric.stdDev

  /**
   * Returns a sequence of all durations in the timer's sample.
   */
  def values = metric.values.toSeq

  /**
   * Returns an array of durations at the given percentiles.
   */
  def percentiles(percentiles: Double*) = metric.percentiles(percentiles: _*)

  /**
   * Returns the timer's rate unit.
   */
  def rateUnit = metric.rateUnit

  /**
   * Returns the timer's duration unit.
   */
  def durationUnit = metric.durationUnit

  /**
   * Returns the type of events the timer is measuring.
   */
  def eventType = metric.eventType

  /**
   * Returns the fifteen-minute rate of timings.
   */
  def fifteenMinuteRate = metric.fifteenMinuteRate

  /**
   * Returns the five-minute rate of timings.
   */
  def fiveMinuteRate = metric.fiveMinuteRate

  /**
   * Returns the mean rate of timings.
   */
  def meanRate = metric.meanRate

  /**
   * Returns the one-minute rate of timings.
   */
  def oneMinuteRate = metric.oneMinuteRate
}
