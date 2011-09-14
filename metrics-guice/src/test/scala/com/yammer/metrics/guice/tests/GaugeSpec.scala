package com.yammer.metrics.guice.tests

import com.codahale.simplespec.Spec
import org.junit.Test
import com.yammer.metrics.guice.{InstrumentationModule, Gauge}
import com.google.inject.Guice
import com.yammer.metrics.core.{MetricsRegistry, GaugeMetric, MetricName}

class InstrumentedWithGauge {
  @Gauge(name = "things")
  def doAThing() = "poop"
}

class GaugeSpec extends Spec {
  class `Annotating a method as Gauge` {
    val injector = Guice.createInjector(new InstrumentationModule)
    val instance = injector.getInstance(classOf[InstrumentedWithGauge])
    val registry = injector.getInstance(classOf[MetricsRegistry])

    @Test def `creates and calls a meter for the class with the given parameters` = {
      instance.doAThing()

      val gauge = registry.allMetrics.get(new MetricName(classOf[InstrumentedWithGauge], "things"))

      gauge.must(be(notNull))
      gauge.must(beA[GaugeMetric[String]])
      gauge.asInstanceOf[GaugeMetric[String]].value.must(be("poop"))
    }
  }
}
