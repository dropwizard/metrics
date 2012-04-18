package com.yammer.metrics.scala

import collection.JavaConversions._
import java.util.concurrent.TimeUnit
import java.io.File

/**
 * A Scala façade class for Timer.
 */
class Timer(metric: com.yammer.metrics.core.Timer) {
  /**
   * Runs f, recording its duration, and returns the result of f.
   */
  def time[A](f: => A): A = {
    val ctx = metric.time
    try {
      f
    } finally {
      ctx.stop
    }
  }

  /**
   * Adds a recorded duration.
   */
  def update(duration: Long, unit: TimeUnit) {
    metric.update(duration, unit)
  }

  /**
   * Returns a timing [[com.metrics.yammer.core.TimerContext]],
   * which measures an elapsed time in nanoseconds.
   */
  def timerContext() = metric.time()

  /**
   * Returns the number of durations recorded.
   */
  def count = metric.getCount

  /**
   * Clears all recorded durations.
   */
  def clear() { metric.clear() }

  /**
   * Returns the longest recorded duration.
   */
  def max = metric.getMax

  /**
   * Returns the shortest recorded duration.
   */
  def min = metric.getMin

  /**
   * Returns the arithmetic mean of all recorded durations.
   */
  def mean = metric.getMean

  /**
   * Returns the standard deviation of all recorded durations.
   */
  def stdDev = metric.getStdDev

  /**
   * Returns a snapshot of the values in the timer's sample.
   */
  def snapshot = metric.getSnapshot

  /**
   * Returns the timer's rate unit.
   */
  def rateUnit = metric.getRateUnit

  /**
   * Returns the timer's duration unit.
   */
  def durationUnit = metric.getDurationUnit

  /**
   * Returns the type of events the timer is measuring.
   */
  def eventType = metric.getEventType

  /**
   * Returns the fifteen-minute rate of timings.
   */
  def fifteenMinuteRate = metric.getFifteenMinuteRate

  /**
   * Returns the five-minute rate of timings.
   */
  def fiveMinuteRate = metric.getFiveMinuteRate

  /**
   * Returns the mean rate of timings.
   */
  def meanRate = metric.getMeanRate

  /**
   * Returns the one-minute rate of timings.
   */
  def oneMinuteRate = metric.getOneMinuteRate
}

