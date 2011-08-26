package com.yammer.metrics.core.tests

import org.junit.Test
import com.codahale.simplespec.Spec
import com.yammer.metrics.core.GaugeMetric

class GaugeMetricSpec extends Spec {

  class `A gauge metric` {
    val metric = new GaugeMetric[String] {
      def value = "woo"
    }

    @Test def `return a value` = {
      metric.value().must(be("woo"))
    }
  }

}
