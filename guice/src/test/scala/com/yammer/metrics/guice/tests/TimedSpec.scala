package com.yammer.metrics.guice.tests

import com.codahale.simplespec.Spec
import java.util.concurrent.TimeUnit
import com.google.inject.Guice
import com.yammer.metrics.guice.{InstrumentationModule, Timed}
import com.yammer.metrics.Metrics
import com.yammer.metrics.core.{TimerMetric, MetricName}

class InstrumentedWithTimed {
  @Timed(name = "things", rateUnit = TimeUnit.MINUTES, durationUnit = TimeUnit.MICROSECONDS)
  def doAThing() {
    "poop"
  }
}

object TimedSpec extends Spec {
  class `Annotating a method as Timed` {
    private val injector = Guice.createInjector(new InstrumentationModule)
    private val instance = injector.getInstance(classOf[InstrumentedWithTimed])

    def `should create and call a meter for the class with the given parameters` = {
      instance.doAThing()

      val timer = Metrics.allMetrics.get(new MetricName(classOf[InstrumentedWithTimed], "things"))

      timer must not(beNull)
      timer.isInstanceOf[TimerMetric] must beTrue
      timer.asInstanceOf[TimerMetric].count must beEqualTo(1)
      timer.asInstanceOf[TimerMetric].rateUnit must beEqualTo(TimeUnit.MINUTES)
      timer.asInstanceOf[TimerMetric].durationUnit must beEqualTo(TimeUnit.MICROSECONDS)
    }
  }
}
