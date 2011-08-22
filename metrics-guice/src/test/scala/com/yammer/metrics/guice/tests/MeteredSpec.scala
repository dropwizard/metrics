package com.yammer.metrics.guice.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import java.util.concurrent.TimeUnit
import com.google.inject.Guice
import com.yammer.metrics.guice.{InstrumentationModule, Metered}
import com.yammer.metrics.core.{MetricsRegistry, MeterMetric, MetricName}

class InstrumentedWithMetered {
  @Metered(name = "things", eventType = "poops", rateUnit = TimeUnit.MINUTES)
  def doAThing() {
    "poop"
  }
}

class MeteredSpec extends Spec {
  class `Annotating a method as Metered` {
    val injector = Guice.createInjector(new InstrumentationModule)
    val instance = injector.getInstance(classOf[InstrumentedWithMetered])
    val registry = injector.getInstance(classOf[MetricsRegistry])

    @test def `creates and calls a meter for the class with the given parameters` = {
      instance.doAThing()

      val meter = registry.allMetrics.get(new MetricName(classOf[InstrumentedWithMetered], "things"))

      meter.mustBeNotNull()
      meter.mustBeAnInstanceOf[MeterMetric]
      meter.asInstanceOf[MeterMetric].count.mustEqual(1)
      meter.asInstanceOf[MeterMetric].eventType.mustEqual("poops")
      meter.asInstanceOf[MeterMetric].rateUnit.mustEqual(TimeUnit.MINUTES)
    }
  }
}
