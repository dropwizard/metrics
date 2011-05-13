package com.yammer.metrics.stats.tests

import collection.JavaConversions._
import com.codahale.simplespec.Spec
import com.yammer.metrics.stats.ExponentiallyDecayingSample

object ExponentiallyDecayingSampleSpec extends Spec {
  class `A sample of 100 out of 1000 elements` {
    private val population = Range(0, 100).map { _.toLong.asInstanceOf[java.lang.Long] }
    private val sample = new ExponentiallyDecayingSample(1000, 0.99)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    def `should have 100 elements` = {
      sample.size must beEqualTo(100)
      sample.values.toList must haveSize(100)
    }

    def `should only have elements from the population` = {
      (sample.values().toSet -- population.toSet) must beEmpty
    }
  }

  class `A sample of 100 out of 10 elements` {
    private val population = Range(0, 10).map { _.toLong.asInstanceOf[java.lang.Long] }
    private val sample = new ExponentiallyDecayingSample(100, 0.99)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    def `should have 10 elements` = {
      sample.size must beEqualTo(10)
      sample.values.toList must haveSize(10)
    }

    def `should only have elements from the population` = {
      (sample.values().toSet -- population.toSet) must beEmpty
    }
  }

  class `A heavily-biased sample of 100 out of 1000 elements` {
    private val population = Range(0, 100).map { _.toLong.asInstanceOf[java.lang.Long] }
    private val sample = new ExponentiallyDecayingSample(1000, 0.01)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    def `should have 100 elements` = {
      sample.size must beEqualTo(100)
      sample.values.toList must haveSize(100)
    }

    def `should only have elements from the population` = {
      (sample.values().toSet -- population.toSet) must beEmpty
    }
  }
}
