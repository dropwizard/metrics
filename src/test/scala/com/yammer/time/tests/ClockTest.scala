package com.yammer.time.tests


import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterEach, Spec}
import com.yammer.time.Clock

class ClockTest extends Spec with MustMatchers with BeforeAndAfterEach {
  override protected def afterEach() {
    Clock.unfreezeTime()
  }

  describe("the clock") {
    it("returns the current time in nanoseconds (with millisecond precision)") {
      Clock.nanoTime must be(System.nanoTime plusOrMinus 1e6.toLong)
    }
  }

  describe("a frozen clock") {
    it("returns a fixed number") {
      Clock.freezeTime(100)
      Clock.nanoTime must equal(100)
    }
  }

  describe("a unfrozen clock") {
    it("returns the current time again") {
      Clock.freezeTime(100)
      Clock.unfreezeTime()
      Clock.nanoTime must be(System.nanoTime plusOrMinus 1e6.toLong)
    }
  }
}
