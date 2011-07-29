package com.yammer.metrics.guice.tests

import com.codahale.simplespec.Spec
import java.util.concurrent.TimeUnit
import com.google.inject.Guice
import com.yammer.metrics.guice.{InstrumentationModule, Metered}
import com.yammer.metrics.{Metrics, MetricsRegistry}
import com.yammer.metrics.core.{MeterMetric, MetricName}

class InstrumentedWithMetered {
  @Metered(name = "things", eventType = "poops", rateUnit = TimeUnit.MINUTES)
  def doAThing() {
    "poop"
  }
}

class MeteredSpec extends Spec {
  class `Annotating a method as Metered` {
    private val injector = Guice.createInjector(new InstrumentationModule)
    private val instance = injector.getInstance(classOf[InstrumentedWithMetered])
    private val registry = injector.getInstance(classOf[MetricsRegistry])

    def `should create and call a meter for the class with the given parameters` = {
      instance.doAThing()

      val meter = registry.allMetrics.get(new MetricName(classOf[InstrumentedWithMetered], "things"))

      meter must not(beNull)
      meter.isInstanceOf[MeterMetric] must beTrue
      meter.asInstanceOf[MeterMetric].count must beEqualTo(1)
      meter.asInstanceOf[MeterMetric].eventType must beEqualTo("poops")
      meter.asInstanceOf[MeterMetric].rateUnit must beEqualTo(TimeUnit.MINUTES)
    }
  }
}
