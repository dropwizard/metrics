package com.yammer.metrics.core.tests

import com.codahale.simplespec.Spec
import java.util.concurrent.TimeUnit
import com.yammer.metrics.core.MeterMetric

object MeterMetricSpec extends Spec {
  class `A meter metric with no events` {
    val meter = MeterMetric.newMeter("thangs", TimeUnit.SECONDS)

    def `should have a count of zero` {
      meter.count must beEqualTo(0)
    }

    def `should have a mean rate of 0 events/sec` {
      meter.meanRate must beEqualTo(0.0)
    }
  }

  class `A meter metric with three events` {
    val meter = MeterMetric.newMeter("thangs", TimeUnit.SECONDS)
    meter.mark(3)

    def `should have a count of three` {
      meter.count must beEqualTo(3)
    }
  }
}
