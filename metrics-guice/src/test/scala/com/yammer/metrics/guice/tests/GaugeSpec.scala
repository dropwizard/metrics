package com.yammer.metrics.guice.tests

import com.codahale.simplespec.Spec
import com.yammer.metrics.guice.{InstrumentationModule, Gauge}
import com.google.inject.Guice
import com.yammer.metrics.{Metrics, MetricsRegistry}
import com.yammer.metrics.core.{GaugeMetric, MetricName}

class InstrumentedWithGauge {
  @Gauge(name="things")
  def doAThing() = "poop"
}

class GaugeSpec extends Spec {
  class `Annotating a method as Gauge` {
    private val injector = Guice.createInjector(new InstrumentationModule)
    private val instance = injector.getInstance(classOf[InstrumentedWithGauge])
    private val registry = injector.getInstance(classOf[MetricsRegistry])

    def `should create and call a meter for the class with the given parameters` = {
      instance.doAThing()

      val gauge = registry.allMetrics.get(new MetricName(classOf[InstrumentedWithGauge], "things"))

      gauge must not(beNull)
      gauge.isInstanceOf[GaugeMetric[_]] must beTrue
      gauge.asInstanceOf[GaugeMetric[String]].value must beEqualTo("poop")
    }
  }
}
