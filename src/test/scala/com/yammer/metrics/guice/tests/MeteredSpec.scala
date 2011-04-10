package com.yammer.metrics.guice.tests

import com.codahale.simplespec.Spec
import java.util.concurrent.TimeUnit
import com.google.inject.Guice
import com.yammer.metrics.guice.{InstrumentationModule, Metered}
import com.yammer.metrics.Metrics
import com.yammer.metrics.core.MeterMetric
import com.yammer.metrics.NameBuilder

class InstrumentedWithMetered {
  @Metered(name = "things", eventType = "poops", rateUnit = TimeUnit.MINUTES)
  def doAThing() {
    "poop"
  }
}

object MeteredSpec extends Spec {
  class `Annotating a method as Metered` {
    val injector = Guice.createInjector(new InstrumentationModule)
    val instance = injector.getInstance(classOf[InstrumentedWithMetered])

    def `should create and call a meter for the class with the given parameters` {
      instance.doAThing()

      val meter = Metrics.allMetrics.get(NameBuilder(classOf[InstrumentedWithMetered])("things"))

      meter must not(beNull)
      meter.isInstanceOf[MeterMetric] must beTrue
      meter.asInstanceOf[MeterMetric].count must beEqualTo(1)
      meter.asInstanceOf[MeterMetric].eventType must beEqualTo("poops")
      meter.asInstanceOf[MeterMetric].rateUnit must beEqualTo(TimeUnit.MINUTES)
    }
  }
}
