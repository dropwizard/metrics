package com.yammer.metrics.tests


import org.scalatest.matchers.MustMatchers
import java.util.concurrent.TimeUnit
import org.scalatest.{BeforeAndAfterEach, Spec}
import com.yammer.time.Clock
import com.yammer.metrics.Meter

class MeterTest extends Spec with MustMatchers with BeforeAndAfterEach {
  override protected def afterEach() {
    Clock.unfreezeTime()
  }

  describe("a meter with two events in a second") {
    Clock.freezeTime(100)
    val meter = new Meter
    meter.mark(2)
    Clock.freezeTime(100 + TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS))

    it("has a rate of two events per second") {
      meter.rate(TimeUnit.SECONDS) must be (2.0 plusOrMinus 0.0000001)
    }

    it("has two events") {
      meter.count must be(2)
    }
  }

  describe("a meter with no events") {
    val meter = new Meter

    it("has a rate of zero") {
      meter.rate(TimeUnit.DAYS) must be (0.0 plusOrMinus 0.0000001)
    }
  }

  describe("a meter with four marked events and three unmarked events") {
    val meter = new Meter
    meter.mark(3)
    meter.unmark()
    meter.unmark(2)
    meter.mark()
    
    it("has one event") {
      meter.count must be(1)
    }
  }
}
