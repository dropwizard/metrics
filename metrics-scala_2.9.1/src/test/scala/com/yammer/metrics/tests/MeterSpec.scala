package com.yammer.metrics.tests

import org.junit.Test
import com.codahale.simplespec.Spec
<<<<<<< HEAD
=======
import org.junit.Test
>>>>>>> hotfix/2.0.0-BETA16-with-2.9.1
import com.yammer.metrics.core.MeterMetric
import com.yammer.metrics.Meter

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
