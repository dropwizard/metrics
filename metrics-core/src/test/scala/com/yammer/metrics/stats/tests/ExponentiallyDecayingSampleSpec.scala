package com.yammer.metrics.stats.tests

import collection.JavaConversions._
import com.codahale.simplespec.Spec
import com.yammer.metrics.stats.ExponentiallyDecayingSample

class ExponentiallyDecayingSampleSpec extends Spec {
  class `A sample of 100 out of 1000 elements` {
    private val population = Range(0, 100).map { _.toLong.asInstanceOf[java.lang.Long] }
    private val sample = new ExponentiallyDecayingSample(1000, 0.99)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    def `should have 100 elements` = {
      sample.size.mustEqual(100)
      sample.values.toList.mustHaveSize(100)
    }

    def `should only have elements from the population` = {
      (sample.values().toSet -- population.toSet).mustBeEmpty()
    }
  }

  class `A sample of 100 out of 10 elements` {
    private val population = Range(0, 10).map { _.toLong.asInstanceOf[java.lang.Long] }
    private val sample = new ExponentiallyDecayingSample(100, 0.99)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    def `should have 10 elements` = {
      sample.size.mustEqual(10)
      sample.values.toList.mustHaveSize(10)
    }

    def `should only have elements from the population` = {
      (sample.values().toSet -- population.toSet).mustBeEmpty()
    }
  }

  class `A heavily-biased sample of 100 out of 1000 elements` {
    private val population = Range(0, 100).map { _.toLong.asInstanceOf[java.lang.Long] }
    private val sample = new ExponentiallyDecayingSample(1000, 0.01)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    def `should have 100 elements` = {
      sample.size.mustEqual(100)
      sample.values.toList.mustHaveSize(100)
    }

    def `should only have elements from the population` = {
      (sample.values().toSet -- population.toSet).mustBeEmpty()
    }
  }
}
