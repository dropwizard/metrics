package com.yammer.jmx.metrics

import java.util.concurrent.atomic.AtomicLong

/**
 * A thread-safe counter which can go up or down from its initial value.
 */
class Counter(initial: Long) {
  protected val value = new AtomicLong(initial)

  /**
   * Creates a new counter with an initial value of zero.
   */
  def this() = this(0)

  /**
   * Increments the counter by one.
   */
  def inc() {
    inc(1)
  }

  /**
   * Increments the counter by an arbitrary amount.
   */
  def inc(delta: Long) {
    value.getAndAdd(delta)
  }

  /**
   * Decrements the counter by one.
   */
  def dec() {
    dec(1)
  }

  /**
   * Decrements the counter by an arbitrary amount.
   */
  def dec(delta: Long) {
    inc(0 - delta)
  }

  /**
   * Returns the current count.
   */
  def count = value.get
}
