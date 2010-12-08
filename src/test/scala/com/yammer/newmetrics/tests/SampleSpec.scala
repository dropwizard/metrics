package com.yammer.newmetrics.tests

import collection.JavaConversions._
import com.codahale.simplespec.Spec
import com.yammer.newmetrics.Sample

object SampleSpec extends Spec {
  class `A sample of 100 out of 1000 elements` {
    val population = Range(0, 1000)
    val sample = new Sample(100)
    population.foreach { i => sample.update(i) }

    def `should have 100 elements` {
      sample.size must beEqualTo(100)
      sample.toList must haveSize(100)
    }

    def `should only have elements from the population` {
      population must containAll(sample)
    }
  }

  class `A sample of 100 out of 10 elements` {
    val population = Range(0, 10)
    val sample = new Sample(100)
    population.foreach { i => sample.update(i) }

    def `should have 10 elements` {
      sample.size must beEqualTo(10)
      sample.toList must haveSize(10)
    }

    def `should only have elements from the population` {
      population must containAll(sample)
    }
  }
}
