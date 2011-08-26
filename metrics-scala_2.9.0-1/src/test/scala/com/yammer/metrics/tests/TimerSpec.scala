package com.yammer.metrics.tests

import org.junit.Test
import com.codahale.simplespec.Spec
import com.yammer.metrics.core.TimerMetric
import com.yammer.metrics.Timer
import java.util.concurrent.TimeUnit


class TimerSpec extends Spec {
  class `A timer` {
    val metric = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.SECONDS)
    val timer = new Timer(metric)

    @Test def `updates the underlying metric` = {
      timer.time { Thread.sleep(100); 10 }.must(be(10))

      metric.min().must(be(approximately(100.0, 10)))
    }
  }
}
