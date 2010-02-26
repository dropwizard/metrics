package com.yammer.time.tests


import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import java.util.concurrent.TimeUnit
import com.yammer.time.{Duration, Rate}

class RateTest extends Spec with MustMatchers {
  val precision = 1e-5

  describe("a rate of 3 events per nanosecond") {
    val r = Rate.inNanos(3)

    it("is in nanoseconds") {
      r.value must be(3.0 plusOrMinus precision)
      r.unit must be(TimeUnit.NANOSECONDS)
    }

    it("is human-readable") {
      r.toString must equal("3.00/ns")
    }
  }

  describe("a rate of 3 events per microsecond") {
    val r = Rate.inMicros(3)

    it("is in microseconds") {
      r.value must be(3.0 plusOrMinus precision)
      r.unit must be(TimeUnit.MICROSECONDS)
    }

    it("is human-readable") {
      r.toString must equal("3.00/us")
    }
  }

  describe("a rate of 3 events per millisecond") {
    val r = Rate.inMillis(3)

    it("is in milliseconds") {
      r.value must be(3.0 plusOrMinus precision)
      r.unit must be(TimeUnit.MILLISECONDS)
    }

    it("is human-readable") {
      r.toString must equal("3.00/ms")
    }
  }

  describe("a rate of 3 events per second") {
    val r = Rate.inSeconds(3)

    it("is in seconds") {
      r.value must be(3.0 plusOrMinus precision)
      r.unit must be(TimeUnit.SECONDS)
    }

    it("is human-readable") {
      r.toString must equal("3.00/s")
    }
  }

  describe("a rate of 3 events per minute") {
    val r = Rate.inMinutes(3)

    it("is in minutes") {
      r.value must be(3.0 plusOrMinus precision)
      r.unit must be(TimeUnit.MINUTES)
    }

    it("is equal to 4,320 events per day") {
      r.d.value must be(4320.0 plusOrMinus precision)
      r.d.unit must be(TimeUnit.DAYS)
    }

    it("is equal to 180 events per hour") {
      r.h.value must be(180.0 plusOrMinus precision)
      r.h.unit must be(TimeUnit.HOURS)
    }

    it("is equal to 3 events per minute") {
      r.m.value must be(3.0 plusOrMinus precision)
      r.m.unit must be(TimeUnit.MINUTES)
    }

    it("is equal to 0.05 events per second") {
      r.s.value must be(0.05 plusOrMinus precision)
      r.s.unit must be(TimeUnit.SECONDS)
    }

    it("is equal to 0.00005 events per millisecond") {
      r.ms.value must be(5e-5 plusOrMinus precision)
      r.ms.unit must be(TimeUnit.MILLISECONDS)
    }

    it("is equal to 0.00000005 events per microsecond") {
      r.us.value must be(5e-8 plusOrMinus precision)
      r.us.unit must be(TimeUnit.MICROSECONDS)
    }

    it("is equal to 0.00000000005 events per nanosecond") {
      r.ns.value must be(5e-11 plusOrMinus precision)
      r.ns.unit must be(TimeUnit.NANOSECONDS)
    }

    it("is human-readable") {
      r.toString must equal("3.00/min")
    }
  }

  describe("a rate of 3 events per hour") {
    val r = Rate.inHours(3)

    it("is in hours") {
      r.value must be(3.0 plusOrMinus precision)
      r.unit must be(TimeUnit.HOURS)
    }

    it("is human-readable") {
      r.toString must equal("3.00/h")
    }
  }

  describe("a rate of 3 events per day") {
    val r = Rate.inDays(3)

    it("is in days") {
      r.value must be(3.0 plusOrMinus precision)
      r.unit must be(TimeUnit.DAYS)
    }

    it("is human-readable") {
      r.toString must equal("3.00/d")
    }
  }
}
