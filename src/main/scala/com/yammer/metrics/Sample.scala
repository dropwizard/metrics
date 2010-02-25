package com.yammer.metrics

import util.Random
import java.util.concurrent.atomic.AtomicLong
import collection.generic.Growable

/**
 * A random sample of a stream. Uses Vitter's Algorithm R to produce a
 * statistically representative sample.
 *
 * @author coda
 * @see <a href="http://www.cs.umd.edu/~samir/498/vitter.pdf">Random Sampling with a Reservoir</a>
 */
class Sample[A](val window: Int)
               (init: => A)
               (implicit evidence : ClassManifest[A])
        extends Iterable[A] with Growable[A] {

  private val random = new Random()
  private val values = Array.fill(window)(init)
  private val count = new AtomicLong(0)

  /**
   * Returns the number of values recorded.
   */
  override def size = count.get.toInt.min(window)

  /**
   * Clears all recorded values.
   */
  def clear() {
    for (i <- 0 until values.size) {
      values(i) = init
    }
    count.set(0)
  }

  /**
   * Adds a new recorded value.
   */
  def +=(elem : A): this.type = {
    val c = count.incrementAndGet
    if (c < window) {
      values(c.toInt - 1) = elem
    } else {
      val r = random.nextLong.abs % c
      if (r < window) {
        values(r.toInt) = elem
      }
    }
    this
  }

  /**
   * Returns an iterator for the sample set.
   */
  def iterator = values.take(size).iterator
}
