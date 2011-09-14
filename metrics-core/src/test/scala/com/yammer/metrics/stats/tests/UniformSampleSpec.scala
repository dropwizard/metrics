package com.yammer.metrics.stats.tests

import scala.collection.JavaConversions._
import com.codahale.simplespec.Spec
import org.junit.Test
import com.yammer.metrics.stats.UniformSample

class UniformSampleSpec extends Spec {
  class `A sample of 100 out of 1000 elements` {
    val population = Range(0, 1000).map { _.toLong.asInstanceOf[java.lang.Long] }
    val sample = new UniformSample(100)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    @Test def `has 100 elements` = {
      sample.size.must(be(100))
      sample.values.toList.must(haveSize(100))
    }

    @Test def `only has elements from the population` = {
      (sample.values().toSet -- population.toSet).must(be(empty))
    }
  }

  class `A sample of 100 out of 10 elements` {
    val population = Range(0, 10).map { _.toLong.asInstanceOf[java.lang.Long] }
    val sample = new UniformSample(100)
    population.foreach { i => sample.update(i.asInstanceOf[Long]) }

    @Test def `has 10 elements` = {
      sample.size.must(be(10))
      sample.values.toList.must(haveSize(10))
    }

    @Test def `only has elements from the population` = {
      (sample.values().toSet -- population.toSet).must(be(empty))
    }
  }
}
