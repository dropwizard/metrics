package com.yammer.metrics.tests

import com.codahale.simplespec.Spec
import org.specs.mock.Mockito
import com.yammer.metrics.Counter
import com.yammer.metrics.core.CounterMetric

object CounterSpec extends Spec with Mockito {
  class `A counter` {
    val metric = mock[CounterMetric]
    val counter = new Counter(metric)
    
    def `should increment the underlying metric by an arbitrary amount` {
      counter += 12

      there was one(metric).inc(12)
    }

    def `should decrement the underlying metric by an arbitrary amount` {
      counter -= 12

      there was one(metric).dec(12)
    }
  }
}
//
