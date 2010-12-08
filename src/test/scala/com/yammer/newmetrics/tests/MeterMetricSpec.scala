package com.yammer.newmetrics.tests

import com.codahale.simplespec.Spec
import com.yammer.newmetrics.MeterMetric
import java.util.concurrent.TimeUnit
import collection.mutable.ArrayBuffer
import org.specs.matcher.Matcher

object MeterMetricSpec extends Spec {
  class `A meter's one-minute rate` {
    def `should decrease exponentially` {
      rates { _.oneMinuteRate(TimeUnit.SECONDS) } must eventually(
        haveElementsCloseTo(List(0.0, 0.0, 0.0, 0.0, 0.4, 0.4, 0.4, 0.4, 0.4,
                                 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)))
    }
  }

  class `A meter's five-minute rate` {
    def `should decrease exponentially` {
      rates { _.fiveMinuteRate(TimeUnit.SECONDS) } must eventually(
        haveElementsCloseTo(List(0.0, 0.0, 0.0, 0.0, 0.4, 0.4, 0.4, 0.4, 0.4,
                                 0.32, 0.32, 0.32, 0.32, 0.32, 0.25)))
    }
  }

  class `A meter's fifteen-minute rate` {
    def `should decrease exponentially` {
      rates { _.fifteenMinuteRate(TimeUnit.SECONDS) } must eventually(
        haveElementsCloseTo(List(0.0, 0.0, 0.0, 0.0, 0.4, 0.4, 0.4, 0.4,
                                 0.4, 0.37, 0.37, 0.37, 0.37, 0.37, 0.35)))
    }
  }

  class `A meter's mean rate` {
    def `should decrease asymptotically` {
      meanRates must eventually(
        haveElementsCloseTo(List(2.0, 1.0, 0.66, 0.5)))
    }
  }

  class `A meter metric with no events` {
    val meter = MeterMetric.newMeter()

    def `should have a count of zero` {
      meter.count must beEqualTo(0)
    }

    def `should have a mean rate of 0 events/sec` {
      meter.meanRate(TimeUnit.SECONDS) must beEqualTo(0.0)
    }
  }

  class `A meter metric with three events` {
    val meter = MeterMetric.newMeter()
    meter.mark(3)

    def `should have a count of three` {
      meter.count must beEqualTo(3)
    }
  }

  def rates[A](f : MeterMetric => A) = {
    val meter = MeterMetric.newMeter(50, TimeUnit.MILLISECONDS)
    meter.mark()
    meter.mark(1)

    val metrics = new ArrayBuffer[A]
    for (i <- 1 to 15) {
      Thread.sleep(10)
      metrics += f(meter)
    }
    metrics.toList
  }

  def meanRates = {
    val meter = MeterMetric.newMeter()
    meter.mark()
    meter.mark(1)

    val metrics = new ArrayBuffer[Double]
    for (i <- 1 to 4) {
      Thread.sleep(1000)
      metrics += meter.meanRate(TimeUnit.SECONDS)
    }
    metrics.toList
  }

  def haveElementsCloseTo(elements: Seq[Double]) = new Matcher[Seq[Double]] {
    def apply(a: => Seq[Double]) = {
      val x: Seq[Double] = a
      val results = a.zip(elements).map { case (f, e) =>
        val m = beCloseTo(e, 0.01)
        m(f)
      }
      (results.forall { case (r, _, _) => r }, "it worked", "%s has elements which aren't close to %s".format(x, elements))
    }
  }
}
