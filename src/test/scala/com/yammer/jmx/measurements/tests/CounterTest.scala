package com.yammer.jmx.measurements.tests

import org.scalatest.matchers.MustMatchers
import com.yammer.jmx.measurements.Counter
import org.scalatest.Spec

class CounterTest extends Spec with MustMatchers {
  describe("a counter of zero") {
    def makeCounter = new Counter(0)

    describe("incremented by one") {
      val counter = makeCounter
      counter.inc()

      it(" equals one") {
        counter.count must equal(1)
      }
    }

    describe("incremented by two") {
      val counter = makeCounter
      counter.inc(2)

      it("equals two") {
        counter.count must equal(2)
      }
    }

    describe("decremented by one") {
      val counter = makeCounter
      counter.dec()

      it("equals negative one") {
        counter.count must equal(-1)
      }
    }

    describe("decremented by two") {
      val counter = makeCounter
      counter.dec(2)

      it("equals negative two") {
        counter.count must equal(-2)
      }
    }
  }

  describe("a counter without an explicit initial value") {
    it("equals one") {
      new Counter().count must equal(0)
    }
  }
}
