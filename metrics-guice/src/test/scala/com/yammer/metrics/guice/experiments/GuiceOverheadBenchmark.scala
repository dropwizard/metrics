package com.yammer.metrics.guice.experiments

import com.google.inject.Guice
import com.yammer.metrics.Instrumented
import com.yammer.metrics.guice.{Timed, InstrumentationModule}
import java.util.concurrent.TimeUnit

class InstrumentedExample extends Instrumented {
  private val timer = metrics.timer("manual")
  def manual = timer.time { Seq.empty }

  @Timed
  def annotated = Seq.empty
}

object GuiceOverheadBenchmark extends Instrumented {
  val manual = metrics.timer("manual", durationUnit = TimeUnit.MICROSECONDS)
  val annotated = metrics.timer("annotated", durationUnit = TimeUnit.MICROSECONDS)

  def main(args: Array[String]) {
    val injector = Guice.createInjector(new InstrumentationModule)

    val example = injector.getInstance(classOf[InstrumentedExample])

    val iterations = 10000000

    for (i <- 1 to iterations) {
      manual.time { example.manual }
    }

    for (i <- 1 to iterations) {
      annotated.time { example.annotated }
    }

    println(manual.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999).toList)
    println(annotated.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999).toList)
  }
}
