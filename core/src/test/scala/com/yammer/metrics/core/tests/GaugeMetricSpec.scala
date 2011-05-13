package com.yammer.metrics.core.tests

import com.codahale.simplespec.Spec
import com.yammer.metrics.core.GaugeMetric

object GaugeMetricSpec extends Spec {

  class `A gauge metric` {
    private val metric = new GaugeMetric[String] {
      def value = "woo"
    }

    def `should return a value` = {
      metric.value() must beEqualTo("woo")
    }
  }

}
