package com.yammer.metrics.core.tests

import scala.collection.JavaConversions._
import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import java.util.concurrent.{Callable, TimeUnit}
import com.yammer.metrics.core.TimerMetric

class TimerMetricSpec extends Spec {
  class `A blank timer` {
    val timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)

    @test def `has a duration unit` = {
      timer.durationUnit.mustEqual(TimeUnit.MILLISECONDS)
    }

    @test def `has a rate unit` = {
      timer.rateUnit.mustEqual(TimeUnit.SECONDS)
    }

    @test def `has a max of zero` = {
      timer.max.mustBeApproximately(0.0, 0.001)
    }

    @test def `has a min of zero` = {
      timer.min.mustBeApproximately(0.0, 0.001)
    }

    @test def `has a mean of zero` = {
      timer.mean.mustBeApproximately(0.0, 0.001)
    }

    @test def `has a count of zero` = {
      timer.count.mustEqual(0)
    }

    @test def `has a standard deviation of zero` = {
      timer.stdDev.mustBeApproximately(0.0, 0.001)
    }

    @test def `has a median/p95/p98/p99/p999 of zero` = {
      val Array(median, p95, p98, p99, p999) = timer.percentiles(0.5, 0.95, 0.98, 0.99, 0.999)
      median.mustBeApproximately(0.0, 0.001)
      p95.mustBeApproximately(0.0, 0.001)
      p98.mustBeApproximately(0.0, 0.001)
      p99.mustBeApproximately(0.0, 0.001)
      p999.mustBeApproximately(0.0, 0.001)
    }

    @test def `has a mean rate of zero` = {
      timer.meanRate.mustBeApproximately(0.0, 0.001)
    }

    @test def `has a one-minute rate of zero` = {
      timer.oneMinuteRate.mustBeApproximately(0.0, 0.001)
    }

    @test def `has a five-minute rate of zero` = {
      timer.fiveMinuteRate.mustBeApproximately(0.0, 0.001)
    }

    @test def `has a fifteen-minute rate of zero` = {
      timer.fifteenMinuteRate.mustBeApproximately(0.0, 0.001)
    }

    @test def `has no values` = {
      timer.values.toList.mustBeEmpty()
    }
  }

  class `Timing a series of events` {
    val timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)
    timer.update(10, TimeUnit.MILLISECONDS)
    timer.update(20, TimeUnit.MILLISECONDS)
    timer.update(20, TimeUnit.MILLISECONDS)
    timer.update(30, TimeUnit.MILLISECONDS)
    timer.update(40, TimeUnit.MILLISECONDS)

    @test def `records the count` = {
      timer.count.mustEqual(5)
    }

    @test def `calculates the minimum duration` = {
      timer.min.mustBeApproximately(10.0, 0.001)
    }

    @test def `calculates the maximum duration` = {
      timer.max.mustBeApproximately(40.0, 0.001)
    }

    @test def `calculates the mean duration` = {
      timer.mean.mustBeApproximately(24.0, 0.001)
    }

    @test def `calculates the standard deviation` = {
      timer.stdDev.mustBeApproximately(11.401, 0.001)
    }

    @test def `calculates the median/p95/p98/p99/p999` = {
      val Array(median, p95, p98, p99, p999) = timer.percentiles(0.5, 0.95, 0.98, 0.99, 0.999)
      median.mustBeApproximately(20.0, 0.001)
      p95.mustBeApproximately(40.0, 0.001)
      p98.mustBeApproximately(40.0, 0.001)
      p99.mustBeApproximately(40.0, 0.001)
      p999.mustBeApproximately(40.0, 0.001)
    }

    @test def `has a series of values` = {
      timer.values.toSet.mustEqual(Set(10, 20, 20, 30, 40))
    }
  }

  class `Timing crazy-variant values` {
    val timer = new TimerMetric(TimeUnit.DAYS, TimeUnit.SECONDS)
    timer.update(Long.MaxValue, TimeUnit.NANOSECONDS)
    timer.update(0, TimeUnit.NANOSECONDS)

    @test def `calculates the standard deviation without overflowing` = {
      timer.stdDev.mustBeApproximately(75485.05, 0.01)
    }
  }

  class `Timing Callable instances` {
    val timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)

    @test def `records the duration of the Callable#call()` = {
      time.eventually { _.mustBeApproximately(50.0, 2) }
    }

    @test def `returns the result of the callable` = {
      timer.time(new Callable[String] {
        def call = {
          "woo"
        }
      }).mustEqual("woo")
    }

    private def time = {
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
