package com.yammer.metrics.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.Counter
import com.yammer.metrics.core.CounterMetric
import org.specs2.mock.Mockito

class CounterSpec extends Spec with Mockito {
  class `A counter` {
    val metric = mock[CounterMetric]
    val counter = new Counter(metric)
    
    @test def `increments the underlying metric by an arbitrary amount` = {
      counter += 12

      there was one(metric).inc(12)
    }

    @test def `decrements the underlying metric by an arbitrary amount` = {
      counter -= 12

      there was one(metric).dec(12)
    }
  }
}
