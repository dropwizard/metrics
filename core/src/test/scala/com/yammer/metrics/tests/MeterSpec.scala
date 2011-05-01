package com.yammer.metrics.tests

import com.codahale.simplespec.Spec
import org.specs.mock.Mockito
import com.yammer.metrics.core.MeterMetric
import com.yammer.metrics.Meter

object MeterSpec extends Spec with Mockito {
  class `A meter` {
    val metric = mock[MeterMetric]
    val meter = new Meter(metric)

    def `should mark the underlying metric` {
      meter.mark()

      there was one(metric).mark()
    }

    def `should mark the underlying metric by an arbitrary amount` {
      meter.mark(12)

      there was one(metric).mark(12)
    }
  }
}
