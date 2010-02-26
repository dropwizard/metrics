package com.yammer.metrics.tests


import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import com.yammer.metrics.WeightedMeter
import java.util.concurrent.TimeUnit

class WeightedMeterTest extends Spec with MustMatchers {
  describe("a one-minute weighted meter") {
    val meter = WeightedMeter.oneMinute

    it("calculates an exponentially decaying average") {
      for (i <- 1 to 100) {
        meter.mark()
      }
      meter.rate.s.value must be(0.0 plusOrMinus 0.00001)
      Thread.sleep(5000)
      meter.rate.s.value must be(18.0 plusOrMinus 5.0)
    }
  }
}
