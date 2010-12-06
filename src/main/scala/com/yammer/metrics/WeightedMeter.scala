package com.yammer.metrics

import java.util.concurrent.TimeUnit
import com.yammer.time.{Clock, Duration, Rate}
import scala.math.exp

object WeightedMeter {
  val interval = Duration.seconds(5)
  private val oneMinuteFactor = 1 / exp(interval.minutes.value)
  private val fiveMinuteFactor = oneMinuteFactor / 5
  private val fifteenMinuteFactor = oneMinuteFactor / 15

  def oneMinute() = new WeightedMeter(oneMinuteFactor, interval)
  def fiveMinute() = new WeightedMeter(fiveMinuteFactor, interval)
  def fifteenMinute() = new WeightedMeter(fifteenMinuteFactor, interval)
}

/**
 * An exponentially-weighted moving average meter. Calculates a moving average
 * of the rate every five seconds.
 *
 * @author coda
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
class WeightedMeter private(factor: Double, interval: Duration) extends Meter {
  private val intervalInTicks = interval.ns.value.toLong
  private var uncounted = 0L
  private var initialized = false
  private var ema = 0.0
  private var lastRecalculation = Clock.nanoTime

  override def mark(count: Long) {
    super.mark(count)
    synchronized {
      recalculateIfNeeded()
      uncounted += count
    }
  }
  
  override def rate = {
    val r = synchronized {
      recalculateIfNeeded()
      ema
    }

    Rate(r, TimeUnit.NANOSECONDS)
  }

  private def recalculateIfNeeded() {
    val gap = Clock.nanoTime - lastRecalculation
    if (gap >= intervalInTicks) {
      for (i <- 1 to (gap / intervalInTicks).toInt) {
        recalculate()
      }
      lastRecalculation = Clock.nanoTime
    }
  }

  private def recalculate() {
    if (initialized) {
      ema += (factor * ((uncounted.toDouble / intervalInTicks) - ema))
    } else {
      ema = uncounted.toDouble / intervalInTicks
      initialized = true
    }
    uncounted = 0L
    lastRecalculation += intervalInTicks
  }

  def unweightedRate = super.rate
}
