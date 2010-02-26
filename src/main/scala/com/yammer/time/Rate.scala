package com.yammer.time

import java.util.concurrent.TimeUnit

/**
 * Rate's companion object with unit-specific factory methods.
 */
object Rate {
  def inNanos(value: Double) = Rate(value, TimeUnit.NANOSECONDS)
  def inMicros(value: Double) = Rate(value, TimeUnit.MICROSECONDS)
  def inMillis(value: Double) = Rate(value, TimeUnit.MILLISECONDS)
  def inSeconds(value: Double) = Rate(value, TimeUnit.SECONDS)
  def inMinutes(value: Double) = Rate(value, TimeUnit.MINUTES)
  def inHours(value: Double) = Rate(value, TimeUnit.HOURS)
  def inDays(value: Double) = Rate(value, TimeUnit.DAYS)
}

/**
 * A rate with a specified unit of time.
 */
case class Rate(value: Double, unit: TimeUnit) extends Convertible[Rate] {
  def convert(u: TimeUnit) = Rate(value * ratio(u), u)
  override def toString = "%2.2f/%s".format(value, abbreviate(unit))
}
