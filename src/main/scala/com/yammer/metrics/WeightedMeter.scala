package com.yammer.metrics

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, TimeUnit}
import com.yammer.time.Rate

object WeightedMeter {
  val interval = 5
  val intervalUnit = TimeUnit.SECONDS
  private val oneMinuteFactor = 1 /
          Math.exp(TimeUnit.SECONDS.convert(interval, intervalUnit) / 60.0)
  private val fiveMinuteFactor = oneMinuteFactor / 5
  private val fifteenMinuteFactor = oneMinuteFactor / 15
  private val executor = Executors.newSingleThreadScheduledExecutor

  def oneMinute() = schedule(new WeightedMeter(oneMinuteFactor))
  def fiveMinute() = schedule(new WeightedMeter(fiveMinuteFactor))
  def fifteenMinute() = schedule(new WeightedMeter(fifteenMinuteFactor))

  private def schedule(meter: WeightedMeter): WeightedMeter = {
    executor.scheduleAtFixedRate(meter, interval, interval, intervalUnit)
    meter
  }
}

/**
 * An exponentially-weighted moving average meter.
 *
 * @author coda
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
class WeightedMeter private(factor: Double) extends Meter with Runnable {
  import WeightedMeter._
  private val uncounted = new AtomicLong(0)
  private val weightedRate = new AtomicLong(0)

  override def mark(count: Long) {
    uncounted.addAndGet(count)
    super.mark(count)
  }
  
  override def rate = Rate(if (weightedRate.get != 0)
    fromLong(weightedRate.get) / interval
  else
    0.0,
  intervalUnit)

  @deprecated("don't update the average yourself")
  def run() {
    var updated = false
    while (!updated) {
      val oldCas = weightedRate.get
      updated = weightedRate.compareAndSet(oldCas, toLong(calculateEMA(fromLong(oldCas), uncounted.getAndSet(0))))
    }
  }

  def unweightedRate = super.rate

  private def calculateEMA(oldEma: Double, n: Long) = oldEma + (factor * (n - oldEma))
  private def fromLong(n: Long) = java.lang.Double.longBitsToDouble(n)
  private def toLong(d: Double) = java.lang.Double.doubleToLongBits(d)
}
