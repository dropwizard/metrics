package com.yammer.metrics.scala.tests

import org.junit.Test
import com.simple.simplespec.Spec
import com.yammer.metrics.Metrics
import com.yammer.metrics.scala.Timer

class TimerSpec extends Spec {
  class `A timer` {
    val metric = Metrics.newTimer(classOf[TimerSpec], "timer")
    val timer = new Timer(metric)

    @Test def `updates the underlying metric` = {
      timer.time { Thread.sleep(100); 10 }.must(be(10))

      metric.min().must(be(approximately(100.0, 10)))
    }
  }
}

