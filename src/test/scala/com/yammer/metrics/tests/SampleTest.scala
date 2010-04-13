package com.yammer.metrics.tests


import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import com.yammer.metrics.Sample

class SampleTest extends Spec with MustMatchers {
  describe("a sample of 100 out of 1000 elements") {
    val elements = Range(0, 1000)
    val sample = new Sample[Int](100)(0)
    sample ++= elements

    it("has 100 elements") {
      sample.iterator.toList must have size (100)
    }

    it("has elements from the population") {
      for (i <- sample.iterator) {
        elements must contain (i)
      }
    }
  }

  describe("a sample with only 10 elements") {
    val elements = Range(0, 10)
    val sample = new Sample[Int](100)(0)
    sample ++= elements

    it("is convertable to an array") {
      sample.toArray.toList must equal(Range(0, 10).toArray.toList)
    }

    it("has 10 elements") {
      sample.iterator.toList must have size (10)
    }
  }
}
