package com.yammer.metrics.scala.tests

import org.junit.Test
import com.codahale.simplespec.Spec
import com.yammer.metrics.core.MeterMetric
import com.yammer.metrics.scala.Meter

class MeterSpec extends Spec {
  class `A meter` {
    val metric = mock[MeterMetric]
    val meter = new Meter(metric)

    @Test def `marks the underlying metric` = {
      meter.mark()

      verify.one(metric).mark()
    }

    @Test def `marks the underlying metric by an arbitrary amount` = {
      meter.mark(12)

      verify.one(metric).mark(12)
    }
  }
}

