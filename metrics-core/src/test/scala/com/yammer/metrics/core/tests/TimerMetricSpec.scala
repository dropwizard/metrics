package com.yammer.metrics.core.tests

import scala.collection.JavaConversions._
import org.junit.Test
import com.codahale.simplespec.Spec
import java.util.concurrent.{Callable, TimeUnit}
import com.yammer.metrics.core.TimerMetric

class TimerMetricSpec extends Spec {
  class `A blank timer` {
    val timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)

    @Test def `has a duration unit` = {
      timer.durationUnit.must(be(TimeUnit.MILLISECONDS))
    }

    @Test def `has a rate unit` = {
      timer.rateUnit.must(be(TimeUnit.SECONDS))
    }

    @Test def `has a max of zero` = {
      timer.max.must(be(approximately(0.0, 0.001)))
    }

    @Test def `has a min of zero` = {
      timer.min.must(be(approximately(0.0, 0.001)))
    }

    @Test def `has a mean of zero` = {
      timer.mean.must(be(approximately(0.0, 0.001)))
    }

    @Test def `has a count of zero` = {
      timer.count.must(be(0))
    }

    @Test def `has a standard deviation of zero` = {
      timer.stdDev.must(be(approximately(0.0, 0.001)))
    }

    @Test def `has a median/p95/p98/p99/p999 of zero` = {
      val Array(median, p95, p98, p99, p999) = timer.percentiles(0.5, 0.95, 0.98, 0.99, 0.999)
      median.must(be(approximately(0.0, 0.001)))
      p95.must(be(approximately(0.0, 0.001)))
      p98.must(be(approximately(0.0, 0.001)))
      p99.must(be(approximately(0.0, 0.001)))
      p999.must(be(approximately(0.0, 0.001)))
    }

    @Test def `has a mean rate of zero` = {
      timer.meanRate.must(be(approximately(0.0, 0.001)))
    }

    @Test def `has a one-minute rate of zero` = {
      timer.oneMinuteRate.must(be(approximately(0.0, 0.001)))
    }

    @Test def `has a five-minute rate of zero` = {
      timer.fiveMinuteRate.must(be(approximately(0.0, 0.001)))
    }

    @Test def `has a fifteen-minute rate of zero` = {
      timer.fifteenMinuteRate.must(be(approximately(0.0, 0.001)))
    }

    @Test def `has no values` = {
      timer.values.toList.must(be(empty))
    }
  }

  class `Timing a series of events` {
    val timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)
    timer.update(10, TimeUnit.MILLISECONDS)
    timer.update(20, TimeUnit.MILLISECONDS)
    timer.update(20, TimeUnit.MILLISECONDS)
    timer.update(30, TimeUnit.MILLISECONDS)
    timer.update(40, TimeUnit.MILLISECONDS)

    @Test def `records the count` = {
      timer.count.must(be(5))
    }

    @Test def `calculates the minimum duration` = {
      timer.min.must(be(approximately(10.0, 0.001)))
    }

    @Test def `calculates the maximum duration` = {
      timer.max.must(be(approximately(40.0, 0.001)))
    }

    @Test def `calculates the mean duration` = {
      timer.mean.must(be(approximately(24.0, 0.001)))
    }

    @Test def `calculates the standard deviation` = {
      timer.stdDev.must(be(approximately(11.401, 0.001)))
    }

    @Test def `calculates the median/p95/p98/p99/p999` = {
      val Array(median, p95, p98, p99, p999) = timer.percentiles(0.5, 0.95, 0.98, 0.99, 0.999)
      median.must(be(approximately(20.0, 0.001)))
      p95.must(be(approximately(40.0, 0.001)))
      p98.must(be(approximately(40.0, 0.001)))
      p99.must(be(approximately(40.0, 0.001)))
      p999.must(be(approximately(40.0, 0.001)))
    }

    @Test def `has a series of values` = {
      timer.values.toSet.must(be(Set[java.lang.Double](10, 20, 20, 30, 40)))
    }
  }

  class `Timing crazy-variant values` {
    val timer = new TimerMetric(TimeUnit.DAYS, TimeUnit.SECONDS)
    timer.update(Long.MaxValue, TimeUnit.NANOSECONDS)
    timer.update(0, TimeUnit.NANOSECONDS)

    @Test def `calculates the standard deviation without overflowing` = {
      timer.stdDev.must(be(approximately(75485.05, 0.01)))
    }
  }

  class `Timing Callable instances` {
    val timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)

    @Test def `records the duration of the Callable#call()` = {
      eventually {
        time
      }.must(be(approximately(50.0, 2)))
    }

    @Test def `returns the result of the callable` = {
      timer.time(new Callable[String] {
        def call = {
          "woo"
        }
      }).must(be("woo"))
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
