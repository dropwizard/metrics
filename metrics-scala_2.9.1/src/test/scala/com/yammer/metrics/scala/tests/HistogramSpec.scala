package com.yammer.metrics.scala.tests

import org.junit.Test
import com.simple.simplespec.Spec
import org.mockito.Mockito._
import com.yammer.metrics.scala.Histogram

class HistogramSpec extends Spec {
  class `An histogram` {
    val metric = mock[com.yammer.metrics.core.Histogram]
    val histogram = new Histogram(metric)

    @Test def `increments the underlying metric by an arbitrary amount` = {
      histogram += 12

      verify.one(metric).update(12)
    }

    @Test def `obtains the mean of the underlying metric` = {
      histogram += 10
      histogram += 5
      histogram += 20
      histogram.mean

      verify.exactly(3)(metric).update(any)
      verify.one(metric).getMean
    }

    @Test def `obtains the standard deviation of the underlying metric` = {
      histogram += 10
      histogram += 5
      histogram += 20
      histogram.stdDev

      verify.exactly(3)(metric).update(any)
      verify.one(metric).getStdDev
    }
  }
}


