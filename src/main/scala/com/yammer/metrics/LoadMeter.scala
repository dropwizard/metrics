package com.yammer.metrics

/**
 * A top-like meter with one-, five-, and fifteen-minute weighted meters.
 */
class LoadMeter extends Meter {
  private val oneMinute = WeightedMeter.oneMinute
  private val fiveMinute = WeightedMeter.fiveMinute
  private val fifteenMinute = WeightedMeter.fifteenMinute

  override def mark(count: Long) {
    super.mark(count)
    oneMinute.mark(count)
    fiveMinute.mark(count)
    fifteenMinute.mark(count)
  }

  def oneMinuteRate = oneMinute.rate
  def fiveMinuteRate = fiveMinute.rate
  def fifteenMinuteRate = fifteenMinute.rate
}
