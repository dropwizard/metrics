package com.yammer.metrics.core.tests

import com.codahale.simplespec.Spec
import com.yammer.metrics.core.HistogramMetric

object HistogramMetricSpec extends Spec {
  class `A histogram with zero recorded valeus` {
    val histogram = new HistogramMetric(100)

    def `should have a count of 0` {
      histogram.count must beEqualTo(0)
    }

    def `should have a max of 0` {
      histogram.max must beEqualTo(0)
    }

    def `should have a min of 0` {
      histogram.min must beEqualTo(0)
    }

    def `should have a mean of 0` {
      histogram.mean must beCloseTo(0.0, 0.0)
    }

    def `should have a standard deviation of 0` {
      histogram.stdDev must beCloseTo(0.0, 0.0)
    }

    def `should calculate percentiles` {
      val percentiles = histogram.percentiles(0.5, 0.75, 0.99)

      percentiles(0) must beCloseTo(0.0, 0.01)
      percentiles(1) must beCloseTo(0.0, 0.01)
      percentiles(2) must beCloseTo(0.0, 0.01)
    }
  }

  class `A histogram of the numbers 1 through 10000` {
    val histogram = new HistogramMetric(100000)
    (1 to 10000).foreach(histogram.update)

    def `should have a count of 10000` {
      histogram.count must beEqualTo(10000)
    }

    def `should have a max value of 10000` {
      histogram.max must beEqualTo(10000)
    }

    def `should have a min value of 1` {
      histogram.min must beEqualTo(1)
    }

    def `should have a mean value of 5000.5` {
      histogram.mean must beCloseTo(5000.5, 0.01)
    }

    def `should have a standard deviation of X` {
      histogram.stdDev must beCloseTo(2886.89, 0.1)
    }

    def `should calculate percentiles` {
      val percentiles = histogram.percentiles(0.5, 0.75, 0.99)

      percentiles(0) must beCloseTo(5000.5, 0.01)
      percentiles(1) must beCloseTo(7500.75, 0.01)
      percentiles(2) must beCloseTo(9900.99, 0.01)
    }
  }

}
