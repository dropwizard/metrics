package com.yammer.metrics

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
}
