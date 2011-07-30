package com.yammer.metrics.stats.tests

import scala.collection.JavaConversions._
import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.stats.UniformSample

class UniformSampleSpec extends Spec {
  class `A sample of 100 out of 1000 elements` {
    val population = Range(0, 1000).map { _.toLong.asInstanceOf[java.lang.Long] }
    val sample = new UniformSample(100)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    @test def `has 100 elements` = {
      sample.size must beEqualTo(100)
      sample.values.toList must haveSize(100)
    }

    @test def `only has elements from the population` = {
      (sample.values().toSet -- population.toSet) must beEmpty
    }
  }

  class `A sample of 100 out of 10 elements` {
    val population = Range(0, 10).map { _.toLong.asInstanceOf[java.lang.Long] }
    val sample = new UniformSample(100)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    @test def `has 10 elements` = {
      sample.size must beEqualTo(10)
      sample.values.toList must haveSize(10)
    }

    @test def `only has elements from the population` = {
      (sample.values().toSet -- population.toSet) must beEmpty
    }
  }
}
