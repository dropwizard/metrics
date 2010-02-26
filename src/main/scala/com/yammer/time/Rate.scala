package com.yammer.time

import java.util.concurrent.TimeUnit

/**
 * Rate's companion object with unit-specific factory methods.
 */
object Rate {
  def perNanosecond(value: Double) = Rate(value, TimeUnit.NANOSECONDS)
  def perMicrosecond(value: Double) = Rate(value, TimeUnit.MICROSECONDS)
  def perMillisecond(value: Double) = Rate(value, TimeUnit.MILLISECONDS)
  def perSecond(value: Double) = Rate(value, TimeUnit.SECONDS)
  def perMinute(value: Double) = Rate(value, TimeUnit.MINUTES)
  def perHour(value: Double) = Rate(value, TimeUnit.HOURS)
  def perDay(value: Double) = Rate(value, TimeUnit.DAYS)
}

/**
 * A rate with a specified unit of time.
 */
case class Rate(value: Double, unit: TimeUnit) extends Convertible[Rate] {
  def convert(u: TimeUnit) = Rate(value * ratio(u), u)
  override def toString = "%2.2f/%s".format(value, abbreviate(unit))
}
