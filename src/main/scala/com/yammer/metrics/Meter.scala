package com.yammer.metrics

import java.util.concurrent.TimeUnit
import com.yammer.time.Clock
import java.util.concurrent.atomic.AtomicLong

/**
 * A meter which measures the rate of events occuring in time.
 *
 * @author coda
 */
class Meter {
  private val counter = new AtomicLong
  private val startTime = Clock.nanoTime

  /**
   * Mark the occurence of an event.
   */
  def mark() {
    mark(1)
  }

  /**
   * Mark the occurence of an arbitrary number of events.
   */
  def mark(count: Long) {
    counter.addAndGet(count)
  }

  /**
   * Unmark the occurence of an event.
   */
  def unmark() {
    mark(-1)
  }

  /**
   * Unmark the occurence of an arbitrary number of events.
   */
  def unmark(count: Long) {
    mark(-count)
  }

  /**
   * Returns the number of events marked.
   */
  def count = counter.get

  /**
   * Returns the rate of events in the given unit of time.
   */
  def rate(unit: TimeUnit) = if (count > 0)
    count / ((Clock.nanoTime - startTime) / ratio(unit))
  else 0.0

  private def ratio(unit: TimeUnit) = TimeUnit.NANOSECONDS.convert(1, unit).toDouble
}
