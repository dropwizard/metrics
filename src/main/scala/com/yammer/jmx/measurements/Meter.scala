package com.yammer.jmx.measurements

import java.util.concurrent.TimeUnit

/**
 * A meter which measures the rate of events occuring in time.
 *
 * @author coda
 */
class Meter {
  private val counter = new Counter
  private val startTime = Clock.nanoTime

  /**
   * Mark the occurence of an event.
   */
  def mark() {
    counter.inc()
  }

  /**
   * Mark the occurence of an arbitrary number of events.
   */
  def mark(count: Long) {
    counter.inc(count)
  }

  /**
   * Unmark the occurence of an event.
   */
  def unmark() {
    counter.dec()
  }

  /**
   * Unmark the occurence of an arbitrary number of events.
   */
  def unmark(count: Long) {
    counter.dec(count)
  }

  /**
   * Returns the number of events marked.
   */
  def count = counter.count

  /**
   * Returns the rate of events in the given unit of time.
   */
  def rate(unit: TimeUnit) = if (count > 0)
    count / ((Clock.nanoTime - startTime) / ratio(unit))
  else 0.0

  private def ratio(unit: TimeUnit) = TimeUnit.NANOSECONDS.convert(1, unit).toDouble
}
