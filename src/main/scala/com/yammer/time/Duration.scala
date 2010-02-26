package com.yammer.time

import java.util.concurrent.TimeUnit

/**
 * Duration's companion object with unit-specific factory methods.
 */
object Duration {
  def inNanos(value: Double) = Duration(value, TimeUnit.NANOSECONDS)
  def inMicros(value: Double) = Duration(value, TimeUnit.MICROSECONDS)
  def inMillis(value: Double) = Duration(value, TimeUnit.MILLISECONDS)
  def inSeconds(value: Double) = Duration(value, TimeUnit.SECONDS)
  def inMinutes(value: Double) = Duration(value, TimeUnit.MINUTES)
  def inHours(value: Double) = Duration(value, TimeUnit.HOURS)
  def inDays(value: Double) = Duration(value, TimeUnit.DAYS)
}

/**
 * A length of time with a specified unit.
 */
case class Duration(value: Double, unit: TimeUnit) extends Convertible[Duration] {
  def convert(u: TimeUnit) = Duration(value / ratio(u), u)
  override def toString = "%2.2f%s".format(value, abbreviate(unit))
}
