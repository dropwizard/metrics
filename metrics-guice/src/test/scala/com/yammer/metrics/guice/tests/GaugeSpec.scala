package com.yammer.metrics.guice.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.guice.{InstrumentationModule, Gauge}
import com.google.inject.Guice
import com.yammer.metrics.MetricsRegistry
import com.yammer.metrics.core.{GaugeMetric, MetricName}

class InstrumentedWithGauge {
  @Gauge(name = "things")
  def doAThing() = "poop"
}

class GaugeSpec extends Spec {
  class `Annotating a method as Gauge` {
    val injector = Guice.createInjector(new InstrumentationModule)
    val instance = injector.getInstance(classOf[InstrumentedWithGauge])
    val registry = injector.getInstance(classOf[MetricsRegistry])

    @test def `creates and calls a meter for the class with the given parameters` = {
      instance.doAThing()

      val gauge = registry.allMetrics.get(new MetricName(classOf[InstrumentedWithGauge], "things"))

      gauge must not(beNull)
      gauge.isInstanceOf[GaugeMetric[_]] must beTrue
      gauge.asInstanceOf[GaugeMetric[String]].value must beEqualTo("poop")
    }
  }
}
