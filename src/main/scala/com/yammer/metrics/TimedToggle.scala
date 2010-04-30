package com.yammer.metrics

import com.yammer.time.Duration

/**
 * A class which returns a specific value for a given duration, then returns
 * another specific value for the rest of the object's lifetime.
 *
 * Useful for exposing "freshly deployed" statistics.
 *
 * @author coda
 */
class TimedToggle[A](on: A, off: A, duration: Duration) {
  private val offTime = System.currentTimeMillis + duration.ms.value.toLong

  def get = if (offTime < System.currentTimeMillis) off else on
}
