package com.yammer.metrics

import collection.generic.Growable
import scala.math.floor

/**
 * Calculates the value of the given percentile (0..100) for a sample of the
 * data set.
 *
 * @author coda
 * @see Sample
 */
class Percentile(val sampleSize: Int)
        extends Growable[Double] {
  

  private val sample = new Sample(sampleSize)(0.0)

  /**
   * Adds a value to the data set.
   */
  def +=(x: Double): this.type = {
    require(x >= 0)
    synchronized {
      sample += x
    }
    this
  }

  /**
   * Clears the data set.
   */
  def clear() {
    sample.clear()
  }

  /**
   * Returns the value of the given percentile.
   */
  def value(p: Double) = {
    val measurements = synchronized { sample.toArray.sortWith { _ < _ } }
    val pos = (p / 100.0) * (measurements.size + 1)

    if (pos < 1) {
      measurements.head
    } else if (pos >= measurements.size) {
      measurements.last
    } else {
      val lower = measurements(pos.toInt - 1)
      val upper = measurements(pos.toInt)
      lower + (pos - floor(pos)) * (upper - lower)
    }
  }
}
