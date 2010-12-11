package com.yammer.metrics.core.tests

import com.codahale.simplespec.Spec
import com.yammer.metrics.core.ValueMetric

object ValueMetricSpec extends Spec {

  class `A value metric` {
    val metric = new ValueMetric[String] {
      def value = "woo"
    }

    def `should return a value` {
      metric.value() must beEqualTo("woo")
    }
  }

}
