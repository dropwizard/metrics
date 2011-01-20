package com.yammer.metrics.core.tests

import scala.collection.JavaConversions._
import com.codahale.simplespec.Spec
import java.util.concurrent.{Callable, TimeUnit}
import com.yammer.metrics.core.TimerMetric

object TimerMetricSpec extends Spec {
  class `A blank timer` {
    val timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)

    def `should have a duration unit` {
      timer.getDurationUnit must be(TimeUnit.MILLISECONDS)
    }

    def `should have a rate unit` {
      timer.getRateUnit must be(TimeUnit.SECONDS)
    }

    def `should have a max of zero` {
      timer.max must beCloseTo(0.0, 0.001)
    }

    def `should have a min of zero` {
      timer.min must beCloseTo(0.0, 0.001)
    }

    def `should have a mean of zero` {
      timer.mean must beCloseTo(0.0, 0.001)
    }

    def `should have a count of zero` {
      timer.count must beEqualTo(0)
    }

    def `should have a standard deviation of zero` {
      timer.stdDev must beCloseTo(0.0, 0.001)
    }

    def `should have a median/p95/p98/p99/p999 of zero` {
      val Array(median, p95, p98, p99, p999) = timer.percentiles(0.5, 0.95, 0.98, 0.99, 0.999)
      median must beCloseTo(0.0, 0.001)
      p95 must beCloseTo(0.0, 0.001)
      p98 must beCloseTo(0.0, 0.001)
      p99 must beCloseTo(0.0, 0.001)
      p999 must beCloseTo(0.0, 0.001)
    }

    def `should have a mean rate of zero` {
      timer.meanRate must beCloseTo(0.0, 0.001)
    }

    def `should have a one-minute rate of zero` {
      timer.oneMinuteRate must beCloseTo(0.0, 0.001)
    }

    def `should have a five-minute rate of zero` {
      timer.fiveMinuteRate must beCloseTo(0.0, 0.001)
    }

    def `should have a fifteen-minute rate of zero` {
      timer.fifteenMinuteRate must beCloseTo(0.0, 0.001)
    }

    def `should have no values` {
      timer.values.toList must beEmpty
    }
  }

  class `Timing a series of events` {
    val timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)
    timer.update(10, TimeUnit.MILLISECONDS)
    timer.update(20, TimeUnit.MILLISECONDS)
    timer.update(20, TimeUnit.MILLISECONDS)
    timer.update(30, TimeUnit.MILLISECONDS)
    timer.update(40, TimeUnit.MILLISECONDS)

    def `should record the count` {
      timer.count must beEqualTo(5)
    }

    def `should calculate the minimum duration` {
      timer.min must beCloseTo(10.0, 0.001)
    }

    def `should calclate the maximum duration` {
      timer.max must beCloseTo(40.0, 0.001)
    }

    def `should calclate the mean duration` {
      timer.mean must beCloseTo(24.0, 0.001)
    }

    def `should calclate the standard deviation` {
      timer.stdDev must beCloseTo(11.401, 0.001)
    }

    def `should calculate the median/p95/p98/p99/p999` {
      val Array(median, p95, p98, p99, p999) = timer.percentiles(0.5, 0.95, 0.98, 0.99, 0.999)
      median must beCloseTo(20.0, 0.001)
      p95 must beCloseTo(40.0, 0.001)
      p98 must beCloseTo(40.0, 0.001)
      p99 must beCloseTo(40.0, 0.001)
      p999 must beCloseTo(40.0, 0.001)
    }

    def `should have a series of values` {
      timer.values.toList must beEqualTo(Seq(10, 20, 20, 30, 40))
    }
  }

  class `Timing crazy-variant values` {
    val timer = new TimerMetric(TimeUnit.DAYS, TimeUnit.SECONDS)
    timer.update(Long.MaxValue, TimeUnit.NANOSECONDS)
    timer.update(0, TimeUnit.NANOSECONDS)

    def `should calculate the standard deviation without overflowing` {
      timer.stdDev must beCloseTo(75485.05, 0.01)
    }
  }

  class `Timing Callable instances` {
    val timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)

    def `should record the duration of the Callable#call()` {
      time must eventually(beCloseTo(50.0, 1))
    }

    def `should return the result of the callable` {
      timer.time(new Callable[String] {
        def call = {
          "woo"
        }
      }) must beEqualTo("woo")
    }

    def time = {
      timer.time(new Callable[String] {
        def call = {
          Thread.sleep(50)
          "woo"
        }
      })
      timer.max
    }
  }
}
