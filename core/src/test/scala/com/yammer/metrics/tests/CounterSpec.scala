package com.yammer.metrics.tests

import com.codahale.simplespec.Spec
import com.yammer.metrics.Counter
import com.yammer.metrics.core.CounterMetric
import org.specs2.mock.Mockito

object CounterSpec extends Spec with Mockito {
  class `A counter` {
    private val metric = mock[CounterMetric]
    private val counter = new Counter(metric)
    
    def `should increment the underlying metric by an arbitrary amount` = {
      counter += 12

      there was one(metric).inc(12)
    }

    def `should decrement the underlying metric by an arbitrary amount` = {
      counter -= 12

      there was one(metric).dec(12)
    }
  }
}
//
