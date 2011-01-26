package com.yammer.metrics.core.tests

import collection.JavaConversions._
import com.codahale.simplespec.Spec
import com.yammer.metrics.core.ExponentiallyDecayingSample

object ExponentiallyDecayingSampleSpec extends Spec {
  class `A sample of 100 out of 1000 elements` {
    val population = Range(0, 100)
    val sample = new ExponentiallyDecayingSample(1000, 0.99)
    population.foreach(sample.update(_))

    def `should have 100 elements` {
      sample.size must beEqualTo(100)
      sample.values.toList must haveSize(100)
    }

    def `should only have elements from the population` {
      population must containAll(sample.values)
    }
  }

  class `A sample of 100 out of 10 elements` {
    val population = Range(0, 10)
    val sample = new ExponentiallyDecayingSample(100, 0.99)
    population.foreach(sample.update(_))

    def `should have 10 elements` {
      sample.size must beEqualTo(10)
      sample.values.toList must haveSize(10)
    }

    def `should only have elements from the population` {
      population must containAll(sample.values)
    }
  }

  class `A heavily-biased sample of 100 out of 1000 elements` {
    val population = Range(0, 100)
    val sample = new ExponentiallyDecayingSample(1000, 0.01)
    population.foreach(sample.update(_))

    def `should have 100 elements` {
      sample.size must beEqualTo(100)
      sample.values.toList must haveSize(100)
    }

    def `should only have elements from the population` {
      population must containAll(sample.values)
    }
  }
}
