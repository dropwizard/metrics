package com.yammer.metrics.core.tests

import com.codahale.simplespec.Spec
import com.yammer.metrics.core.CounterMetric

class CounterMetricSpec extends Spec {
  class `A counter metric` {
    private val counter = new CounterMetric

    def `should start at zero` = {
      counter.count must beEqualTo(0)
    }

    def `should increment by one` = {
      counter.inc()

      counter.count must beEqualTo(1)
    }

    def `should increment by an arbitrary delta` = {
      counter.inc(3)

      counter.count must beEqualTo(3)
    }

    def `should decrement by one` = {
      counter.dec()

      counter.count must beEqualTo(-1)
    }

    def `should decrement by an arbitrary delta` = {
      counter.dec(3)

      counter.count must beEqualTo(-3)
    }

    def `should be zero after being cleared` = {
      counter.inc(3)
      counter.clear()

      counter.count must beEqualTo(0)
    }
  }

}
