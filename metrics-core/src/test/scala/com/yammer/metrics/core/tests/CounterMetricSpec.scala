package com.yammer.metrics.core.tests

import org.junit.Test
import com.codahale.simplespec.Spec
import com.yammer.metrics.core.CounterMetric

class CounterMetricSpec extends Spec {
  class `A counter metric` {
    val counter = new CounterMetric

    @Test def `starts at zero` = {
      counter.count.must(be(0))
    }

    @Test def `increments by one` = {
      counter.inc()

      counter.count.must(be(1))
    }

    @Test def `increments by an arbitrary delta` = {
      counter.inc(3)

      counter.count.must(be(3))
    }

    @Test def `decrements by one` = {
      counter.dec()

      counter.count.must(be(-1))
    }

    @Test def `decrements by an arbitrary delta` = {
      counter.dec(3)

      counter.count.must(be(-3))
    }

    @Test def `is zero after being cleared` = {
      counter.inc(3)
      counter.clear()

      counter.count.must(be(0))
    }
  }
}
