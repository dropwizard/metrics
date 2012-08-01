package com.yammer.metrics.scala.tests

import org.junit.Test
import com.simple.simplespec.Spec
import com.yammer.metrics.scala.Meter

class MeterSpec extends Spec {
  class `A meter` {
    val metric = mock[com.yammer.metrics.core.Meter]
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

