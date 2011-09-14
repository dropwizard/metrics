package com.yammer.metrics.core.tests

import com.codahale.simplespec.Spec
import org.junit.Test
import java.util.concurrent.TimeUnit
import com.yammer.metrics.core.MeterMetric

class MeterMetricSpec extends Spec {
  class `A meter metric with no events` {
    val meter = MeterMetric.newMeter("thangs", TimeUnit.SECONDS)

    @Test def `has a count of zero` = {
      meter.count.must(be(0))
    }

    @Test def `has a mean rate of 0 events/sec` = {
      meter.meanRate.must(be(0.0))
    }
  }

  class `A meter metric with three events` {
    val meter = MeterMetric.newMeter("thangs", TimeUnit.SECONDS)
    meter.mark(3)

    @Test def `has a count of three` = {
      meter.count.must(be(3))
    }
  }
}
