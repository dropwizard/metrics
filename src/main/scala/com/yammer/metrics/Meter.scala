package com.yammer.metrics

import java.util.concurrent.atomic.AtomicLong
import com.yammer.time.{Rate, Clock}

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
  def rate = Rate.perNanosecond(if (count > 0)
    count.toDouble / (Clock.nanoTime - startTime)
  else 0.0)
}
