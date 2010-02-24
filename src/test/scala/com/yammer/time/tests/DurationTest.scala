package com.yammer.time.tests


import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import com.yammer.time.Duration
import java.util.concurrent.TimeUnit

class DurationTest extends Spec with MustMatchers {
  val precision = 1e-5

  describe("a duration") {
    it("is convertible to a Double") {
      Duration.inNanos(30.2).toDouble must be (30.2 plusOrMinus precision)
    }
  }

  describe("a duration of three nanoseconds") {
    val d = Duration.inNanos(3)

    it("is in nanoseconds") {
      d.magnitude must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.NANOSECONDS)
    }
  }

  describe("a duration of three microseconds") {
    val d = Duration.inMicros(3)

    it("is in microseconds") {
      d.magnitude must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.MICROSECONDS)
    }
  }

  describe("a duration of three milliseconds") {
    val d = Duration.inMillis(3)

    it("is in milliseconds") {
      d.magnitude must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.MILLISECONDS)
    }
  }

  describe("a duration of three seconds") {
    val d = Duration.inSeconds(3)

    it("is in seconds") {
      d.magnitude must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.SECONDS)
    }
  }

  describe("a duration of three minutes") {
    val d = Duration.inMinutes(3)

    it("is in minutes") {
      d.magnitude must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.MINUTES)
    }

    it("is equal to 0.002083 days") {
      d.d.magnitude must be(0.002083 plusOrMinus precision)
      d.d.unit must be(TimeUnit.DAYS)
    }

    it("is equal to 0.05 hours") {
      d.h.magnitude must be(0.05 plusOrMinus precision)
      d.h.unit must be(TimeUnit.HOURS)
    }

    it("is equal to 3 minutes") {
      d.m.magnitude must be(3.0 plusOrMinus precision)
      d.m.unit must be(TimeUnit.MINUTES)
    }

    it("is equal to 180 seconds") {
      d.s.magnitude must be(180.0 plusOrMinus precision)
      d.s.unit must be(TimeUnit.SECONDS)
    }

    it("is equal to 180,000 milliseconds") {
      d.ms.magnitude must be(180000.0 plusOrMinus precision)
      d.ms.unit must be(TimeUnit.MILLISECONDS)
    }

    it("is equal to 180,000,000 microseconds") {
      d.us.magnitude must be(180000000.0 plusOrMinus precision)
      d.us.unit must be(TimeUnit.MICROSECONDS)
    }

    it("is equal to 180,000,000,000 nanoseconds") {
      d.ns.magnitude must be(180000000000.0 plusOrMinus precision)
      d.ns.unit must be(TimeUnit.NANOSECONDS)
    }
  }

  describe("a duration of three hours") {
    val d = Duration.inHours(3)

    it("is in hours") {
      d.magnitude must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.HOURS)
    }
  }

  describe("a duration of three days") {
    val d = Duration.inDays(3)

    it("is in days") {
      d.magnitude must be(3.0 plusOrMinus precision)
      d.unit must be(TimeUnit.DAYS)
    }
  }
}
