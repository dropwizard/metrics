package com.yammer.metrics.tests

import org.scalatest.matchers.MustMatchers
import com.yammer.metrics.TimedToggle
import com.yammer.time.Duration
import org.scalatest.{OneInstancePerTest, Spec}

class TimedToggleTest extends Spec with MustMatchers with OneInstancePerTest {
  describe("a timed toggle") {
    val toggle = new TimedToggle("on", "off", Duration.milliseconds(12))

    it("returns the 'on' value immediately") {
      toggle.get must equal("on")
    }

    it("returns the 'off' value for the rest of the object's lifetime") {
      Thread.sleep(15)
      toggle.get must equal("off")
    }
  }
}
