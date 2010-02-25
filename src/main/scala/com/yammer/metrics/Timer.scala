package com.yammer.metrics

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.TimeUnit
import com.yammer.time.{Duration, Clock}
import collection.generic.Growable

/**
 * A class which tracks the amount of time it takes to perform a particular
 * action and calculates various statistics about the distribution of durations.
 *
 * @author coda
 */
class Timer extends Growable[Duration] {
  private val count_ = new AtomicLong(0)
  private val min_ = new AtomicLong(Long.MaxValue)
  private val max_ = new AtomicLong(Long.MinValue)
  private val sum_ = new AtomicLong(0)
  private val sumOfSquares_ = new AtomicLong(0)
  private val p999_ = new Percentile(99.9, 1000)

  /**
   * Record the amount of time it takes to execute the given function.
   *
   * @return the result of T
   */
  def time[T](f: => T): T = {
    val startTime = Clock.nanoTime
    val t = f
    this += Duration.inNanos(Clock.nanoTime - startTime)
    return t
  }

  /**
   * Returns the number of measurements taken.
   */
  def count = count_.get

  /**
   * Returns the greatest amount of time recorded.
   */
  def max = Duration.inNanos(max_.get)

  /**
   * Returns the least amount of time recorded.
   */
  def min = Duration.inNanos(min_.get)

  /**
   * Returns the arthimetic mean of the recorded durations.
   */
  def mean = Duration.inNanos(sum_.get / count.toDouble)

  /**
   * Returns the standard deviation of the recorded durations.
   */
  def standardDeviation = Duration.inNanos(Math.sqrt(variance))

  /**
   * Returns the duration at the 99.9th percentile.
   */
  def p999 = Duration.inNanos(p999_.value)

  /**
   * Clears all timings.
   */
  def clear() {
    count_.set(0)
    min_.set(Long.MaxValue)
    max_.set(Long.MinValue)
    sum_.set(0)
    sumOfSquares_.set(0)
    p999_.clear()
  }

  /**
   * Adds a duration recorded elsewhere.
   */
  def +=(duration: Duration): this.type = {
    if (duration.magnitude >= 0) {
      val ns = duration.ns.magnitude.toLong
      count_.incrementAndGet
      setMax(ns)
      setMin(ns)
      sum_.getAndAdd(ns)
      sumOfSquares_.getAndAdd(ns * ns)
      p999_ += ns.toDouble
    }
    this
  }

  private def variance = if (count > 1) {
    (sumOfSquares_.get - (sum_.get * mean.magnitude)) / (count - 1).toDouble
  } else {
    0.0
  }

  private def ratio(unit: TimeUnit) = TimeUnit.NANOSECONDS.convert(1, unit).toDouble

  private def setMax(duration: Long) {
    while (max_.get < duration) {
      max_.compareAndSet(max_.get, duration)
    }
  }

  private def setMin(duration: Long) {
    while (min_.get > duration) {
      min_.compareAndSet(min_.get, duration)
    }
  }
}
