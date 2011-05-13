package com.yammer.metrics.tests

import com.codahale.simplespec.Spec
import com.yammer.metrics.core.TimerMetric
import com.yammer.metrics.Timer
import java.util.concurrent.TimeUnit

object TimerSpec extends Spec {
  class `A timer` {
    private val metric = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)
    private val timer = new Timer(metric)

    def `should update the underlying metric` = {
      timer.time { Thread.sleep(100); 10 } must beEqualTo(10)

      metric.min must beCloseTo(100.0, 10)
    }
  }
}
