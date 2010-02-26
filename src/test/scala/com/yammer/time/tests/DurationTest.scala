package com.yammer.time.tests


import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import com.yammer.time.Duration
import java.util.concurrent.TimeUnit

class DurationTest extends Spec with MustMatchers {
  val precision = 1e-5

  describe("a duration of three nanoseconds") {
    val d = Duration.nanoseconds(3)

    it("is in nanoseconds") {
      d.value must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.NANOSECONDS)
    }

    it("is human-readable") {
      d.toString must equal("3.00ns")
    }
  }

  describe("a duration of three microseconds") {
    val d = Duration.microseconds(3)

    it("is in microseconds") {
      d.value must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.MICROSECONDS)
    }

    it("is human-readable") {
      d.toString must equal("3.00us")
    }
  }

  describe("a duration of three milliseconds") {
    val d = Duration.milliseconds(3)

    it("is in milliseconds") {
      d.value must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.MILLISECONDS)
    }

    it("is human-readable") {
      d.toString must equal("3.00ms")
    }
  }

  describe("a duration of three seconds") {
    val d = Duration.seconds(3)

    it("is in seconds") {
      d.value must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.SECONDS)
    }

    it("is human-readable") {
      d.toString must equal("3.00s")
    }
  }

  describe("a duration of three minutes") {
    val d = Duration.minutes(3)

    it("is in minutes") {
      d.value must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.MINUTES)
    }

    it("is equal to 0.002083 days") {
      d.d.value must be(0.002083 plusOrMinus precision)
      d.d.unit must be(TimeUnit.DAYS)
    }

    it("is equal to 0.05 hours") {
      d.h.value must be(0.05 plusOrMinus precision)
      d.h.unit must be(TimeUnit.HOURS)
    }

    it("is equal to 3 minutes") {
      d.m.value must be(3.0 plusOrMinus precision)
      d.m.unit must be(TimeUnit.MINUTES)
    }

    it("is equal to 180 seconds") {
      d.s.value must be(180.0 plusOrMinus precision)
      d.s.unit must be(TimeUnit.SECONDS)
    }

    it("is equal to 180,000 milliseconds") {
      d.ms.value must be(180000.0 plusOrMinus precision)
      d.ms.unit must be(TimeUnit.MILLISECONDS)
    }

    it("is equal to 180,000,000 microseconds") {
      d.us.value must be(180000000.0 plusOrMinus precision)
      d.us.unit must be(TimeUnit.MICROSECONDS)
    }

    it("is equal to 180,000,000,000 nanoseconds") {
      d.ns.value must be(180000000000.0 plusOrMinus precision)
      d.ns.unit must be(TimeUnit.NANOSECONDS)
    }

    it("is human-readable") {
      d.toString must equal("3.00min")
    }
  }

  describe("a duration of three hours") {
    val d = Duration.hours(3)

    it("is in hours") {
      d.value must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.HOURS)
    }

    it("is human-readable") {
      d.toString must equal("3.00h")
    }
  }

  describe("a duration of three days") {
    val d = Duration.days(3)

    it("is in days") {
      d.value must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.DAYS)
    }

    it("is human-readable") {
      d.toString must equal("3.00d")
    }
  }
}
