package com.yammer.metrics.core.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.core.GaugeMetric

class GaugeMetricSpec extends Spec {

  class `A gauge metric` {
    val metric = new GaugeMetric[String] {
      def value = "woo"
    }

    @test def `return a value` = {
      metric.value().must(be("woo"))
    }
  }

}
