package com.yammer.metrics

import util.Random
import collection.generic.Growable
import java.util.concurrent.atomic.{AtomicReferenceArray, AtomicLong}

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
  private val values = new AtomicReferenceArray[A](window)
  private val count = new AtomicLong(0)
  clear()

  /**
   * Returns the number of values recorded.
   */
  override def size = count.get.toInt.min(window)

  /**
   * Clears all recorded values.
   */
  def clear() {
    for (i <- 0 until window) {
      values.set(i, init)
    }
    count.set(0)
  }

  /**
   * Adds a new recorded value.
   */
  def +=(elem : A): this.type = {
    val c = count.incrementAndGet
    if (c < window) {
      values.set(c.toInt - 1, elem)
    } else {
      val r = random.nextLong.abs % c
      if (r < window) {
        values.set(r.toInt, elem)
      }
    }
    this
  }

  /**
   * Returns an iterator for a snapshot of the sample set.
   */
  def iterator = {
    val copy: Array[A] = Array.fill(size)(init)
    for (i <- 0 until size) {
      copy(i) = values.get(i)
    }
    copy.iterator
  }
}
