package com.yammer.jmx.measurements

/**
 * A clock object which returns the current time in nanoseconds for timing
 * purposes. Is freezable for testing purposes.
 *
 * @author coda
 */
object Clock {
  private val UseCurrentTime = 0L
  @volatile private var frozenTime = UseCurrentTime

  /**
   * Returns the current time in nanoseconds.
   *
   * @see System#nanoTime
   */
  def nanoTime = if (frozenTime == UseCurrentTime) {
    System.nanoTime
  } else {
    frozenTime
  }

  /**
   * Freezes the clock such that nanoTime will only return the provided value.
   */
  def freezeTime(at: Long) {
    frozenTime = at
  }

  /**
   * Unfreezes the clock.
   */
  def unfreezeTime() {
    frozenTime = UseCurrentTime
  }
}
