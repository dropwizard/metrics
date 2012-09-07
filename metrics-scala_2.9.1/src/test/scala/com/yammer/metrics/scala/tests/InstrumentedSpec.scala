package com.yammer.metrics.scala.tests

import org.junit.Test
import com.simple.simplespec.Spec
import com.yammer.metrics.scala.Instrumented
import org.hamcrest.Matchers.is
import org.junit.Assert.assertThat
import com.yammer.metrics.Metrics

class InstrumentedSpec extends Spec {
  class `An instrumented object` {
    object Dummy extends Instrumented

    @Test def `has a reference to the default registry` = {
      assertThat(Dummy.metricsRegistry, is(Metrics.defaultRegistry))
    }
  }
}

