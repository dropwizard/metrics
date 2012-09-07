package com.yammer.metrics.scala

import com.yammer.metrics.core.{ Counter => JCounter, Histogram => JHistogram, Meter => JMeter, Timer => JTimer }

/**
 * This class includes some useful implicit conversions similar to the ones in <code>scala.collection.JavaConverters</code>.
 *
 * Usage:
 *  <code> import com.yammer.metrics.scala.Converters._</code>
 *  <code> val counter = Metrics.newCounter(classOf[QueueManager], "pending-jobs").asScala</code>
 *
 * Note: The conversions need to be in scope in order to work (see import in previous example).
 *
 */
object Converters {
  implicit def counter2scala(metric: JCounter) = new Convertible[JCounter, Counter](metric)(new Counter(_))
  implicit def histogram2scala(metric: JHistogram) = new Convertible[JHistogram, Histogram](metric)(new Histogram(_))
  implicit def meter2scala(metric: JMeter) = new Convertible[JMeter, Meter](metric)(new Meter(_))
  implicit def timer2scala(metric: JTimer) = new Convertible[JTimer, Timer](metric)(new Timer(_))
}

class Convertible[T,U](metric: T)(op: (T) => U) {
  def asScala = op(metric)
}