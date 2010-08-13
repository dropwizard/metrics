package com.yammer.metrics.tests

import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import com.yammer.metrics.Percentile

class PercentileTest extends Spec with MustMatchers {
  describe("the 99th percentile of three elements") {
    val percentile = new Percentile(1000)
    percentile ++= List(1.0, 2.0, 3.0)

    it("is the largest element of the set") {
      percentile.value(99) must be(3.0 plusOrMinus 0.00001)
    }
  }

  describe("the 1st percentile of three elements") {
    val percentile = new Percentile(1000)
    percentile ++= List(1.0, 2.0, 3.0)

    it("is the smallest element of the set") {
      percentile.value(1) must be(1.0 plusOrMinus 0.00001)
    }
  }

  describe("the 99th percentile of 1 through 1000") {
    val percentile = new Percentile(10000)
    percentile ++= Range.inclusive(1, 1000).map { _.toDouble }

    it("is the largest element of the set") {
      percentile.value(99) must be(990.99 plusOrMinus 0.00001)
    }
  }
}
