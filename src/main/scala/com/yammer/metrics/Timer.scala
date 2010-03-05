package com.yammer.metrics

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.TimeUnit
import com.yammer.time.{Duration, Clock}
import collection.generic.Growable
import java.lang.Double.{doubleToLongBits, longBitsToDouble}

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
  private val varianceM = new AtomicLong(-1)
  private val varianceS = new AtomicLong(0)
  private val p999_ = new Percentile(99.9, 1000)

  /**
   * Record the amount of time it takes to execute the given function.
   *
   * @return the result of T
   */
  def time[T](f: => T): T = {
    val startTime = Clock.nanoTime
    val t = f
    this += Duration.nanoseconds(Clock.nanoTime - startTime)
    return t
  }

  /**
   * Returns the number of measurements taken.
   */
  def count = count_.get

  /**
   * Returns the greatest amount of time recorded.
   */
  def max = Duration.nanoseconds(max_.get)

  /**
   * Returns the least amount of time recorded.
   */
  def min = Duration.nanoseconds(min_.get)

  /**
   * Returns the arthimetic mean of the recorded durations.
   */
  def mean = Duration.nanoseconds(sum_.get / count.toDouble)

  /**
   * Returns the standard deviation of the recorded durations.
   */
  def standardDeviation = Duration.nanoseconds(Math.sqrt(variance))

  /**
   * Returns the duration at the 99.9th percentile.
   */
  def p999 = Duration.nanoseconds(p999_.value)

  /**
   * Clears all timings.
   */
  def clear() {
    count_.set(0)
    min_.set(Long.MaxValue)
    max_.set(Long.MinValue)
    sum_.set(0)
    varianceM.set(-1)
    varianceS.set(0)
    p999_.clear()
  }

  /**
   * Adds a timing in nanoseconds.
   */
  def +=(ns: Long): this.type = {
    if (ns >= 0) {
      count_.incrementAndGet
      setMax(ns)
      setMin(ns)
      sum_.getAndAdd(ns)
      updateVariance(ns)
      p999_ += ns.toDouble
    }
    this
  }

  /**
   * Adds a duration recorded elsewhere.
   */
  def +=(duration: Duration): this.type = (this += duration.ns.value.toLong)

  private def updateVariance(ns: Long) {
    // initialize varianceM to the first reading if it's still blank
    if (!varianceM.compareAndSet(-1, doubleToLongBits(ns))) {
      var updated = false
      while (!updated) {
        val oldMCas = varianceM.get
        val oldM = longBitsToDouble(oldMCas)
        val newM = oldM + ((ns - oldM) / count)

        val oldSCas = varianceS.get
        val oldS = longBitsToDouble(oldSCas)
        val newS = oldS + ((ns - oldM) * (ns - newM))

        updated = varianceM.compareAndSet(oldMCas, doubleToLongBits(newM)) &&
                  varianceS.compareAndSet(oldSCas, doubleToLongBits(newS))
      }
    }
  }

  private def variance = if (count > 1) {
    longBitsToDouble(varianceS.get) / (count - 1)
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
