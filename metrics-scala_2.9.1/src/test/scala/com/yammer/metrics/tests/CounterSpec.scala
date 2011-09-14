package com.yammer.metrics.tests

import org.junit.Test
import com.codahale.simplespec.Spec
<<<<<<< HEAD
=======
import org.junit.Test
>>>>>>> hotfix/2.0.0-BETA16-with-2.9.1
import com.yammer.metrics.Counter
import com.yammer.metrics.core.CounterMetric

class CounterSpec extends Spec {
  class `A counter` {
    val metric = mock[CounterMetric]
    val counter = new Counter(metric)
    
    @Test def `increments the underlying metric by an arbitrary amount` = {
      counter += 12

      verify.one(metric).inc(12)
    }

    @Test def `decrements the underlying metric by an arbitrary amount` = {
      counter -= 12

      verify.one(metric).dec(12)
    }
  }
}
