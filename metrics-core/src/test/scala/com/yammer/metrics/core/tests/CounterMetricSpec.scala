package com.yammer.metrics.core.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.core.CounterMetric

class CounterMetricSpec extends Spec {
  class `A counter metric` {
    val counter = new CounterMetric

    @test def `starts at zero` = {
      counter.count.must(be(0))
    }

    @test def `increments by one` = {
      counter.inc()

      counter.count.must(be(1))
    }

    @test def `increments by an arbitrary delta` = {
      counter.inc(3)

      counter.count.must(be(3))
    }

    @test def `decrements by one` = {
      counter.dec()

      counter.count.must(be(-1))
    }

    @test def `decrements by an arbitrary delta` = {
      counter.dec(3)

      counter.count.must(be(-3))
    }

    @test def `is zero after being cleared` = {
      counter.inc(3)
      counter.clear()

      counter.count.must(be(0))
    }
  }
}
