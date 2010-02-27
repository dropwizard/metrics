package com.yammer.metrics.tests

import org.scalatest.matchers.MustMatchers
import com.yammer.metrics.WeightedMeter
import com.yammer.time.{Duration, Clock}
import org.scalatest.{BeforeAndAfterEach, Spec}

class WeightedMeterTest extends Spec with MustMatchers with BeforeAndAfterEach {
  override protected def afterEach() {
    Clock.unfreezeTime()
  }

  describe("a load meter") {
    it("calculates an exponentially decaying average at one, five, and fifteen minute settings") {
      Clock.freezeTime(0)
      val meter = WeightedMeter.oneMinute
      // load the meter up with 1 event per ms
      for (i <- 1 to 1000) {
        meter.mark()
        Clock.freezeTime(Clock.nanoTime + Duration.milliseconds(1).ns.value.toLong)
      }

      // wait another 5 seconds
      Clock.freezeTime(Clock.nanoTime + Duration.seconds(5).ns.value.toLong)

      meter.rate.s.value must be(200.0 plusOrMinus 0.1)

      // wait another 5 seconds
      Clock.freezeTime(Clock.nanoTime + Duration.seconds(5).ns.value.toLong)

      meter.rate.s.value must be(15.99 plusOrMinus 0.1)

      // wait another 5 seconds
      Clock.freezeTime(Clock.nanoTime + Duration.seconds(5).ns.value.toLong)

      meter.rate.s.value must be(1.28 plusOrMinus 0.1)
    }
  }
}
