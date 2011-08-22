package com.yammer.metrics.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.core.TimerMetric
import com.yammer.metrics.Timer
import java.util.concurrent.TimeUnit


class TimerSpec extends Spec {
  class `A timer` {
    val metric = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)
    val timer = new Timer(metric)

    @test def `updates the underlying metric` = {
      timer.time { Thread.sleep(100); 10 }.mustEqual(10)

      metric.min().mustBeApproximately(100.0, 10)
    }
  }
}
