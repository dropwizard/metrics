package com.yammer.time

import java.util.concurrent.TimeUnit

/**
 * Duration's companion object with unit-specific factory methods.
 */
object Duration {
  def inNanos(magnitude: Double) = Duration(magnitude, TimeUnit.NANOSECONDS)
  def inMicros(magnitude: Double) = Duration(magnitude, TimeUnit.MICROSECONDS)
  def inMillis(magnitude: Double) = Duration(magnitude, TimeUnit.MILLISECONDS)
  def inSeconds(magnitude: Double) = Duration(magnitude, TimeUnit.SECONDS)
  def inMinutes(magnitude: Double) = Duration(magnitude, TimeUnit.MINUTES)
  def inHours(magnitude: Double) = Duration(magnitude, TimeUnit.HOURS)
  def inDays(magnitude: Double) = Duration(magnitude, TimeUnit.DAYS)
}

/**
 * A length of time with a specified unit.
 */
case class Duration(magnitude: Double, unit: TimeUnit) {
  /**
   * Returns the duration in nanoseconds.
   */
  def ns = convert(TimeUnit.NANOSECONDS)

  /**
   * Returns the duration in microseconds.
   */
  def us = convert(TimeUnit.MICROSECONDS)

  /**
   * Returns the duration in milliseconds.
   */
  def ms = convert(TimeUnit.MILLISECONDS)

  /**
   * Returns the duration in seconds.
   */
  def s  = convert(TimeUnit.SECONDS)

  /**
   * Returns the duration in minutes.
   */
  def m  = convert(TimeUnit.MINUTES)

  /**
   * Returns the duration in hours.
   */
  def h  = convert(TimeUnit.HOURS)

  /**
   * Returns the duration in days.
   */
  def d  = convert(TimeUnit.DAYS)
  
  private def convert(u: TimeUnit) = Duration(magnitude / ratio(u), u)
  private def ratio(u: TimeUnit) = {
    val r = unit.convert(1, u)
    if (r > 0) {
      r.toDouble
    } else {
      1.0 / u.convert(1, unit)
    }
  }
  override def toString = "%f %s".format(magnitude, unit.toString.toLowerCase)
}
