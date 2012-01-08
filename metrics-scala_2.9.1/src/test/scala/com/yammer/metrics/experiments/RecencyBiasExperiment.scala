package com.yammer.metrics.experiments

import collection.JavaConversions._
import java.util.concurrent.TimeUnit
import com.yammer.metrics.stats.{ExponentiallyDecayingSample}
import com.yammer.metrics.stats.{UniformSample}
import java.io.{PrintWriter, FileOutputStream}

/**
 * A simple experiment to see how uniform and exponentially-decaying samples
 * respond to a linearly-increasing set of measurements.
 *
 * For two hours, it measures the number of seconds the test has been running
 * and places that value in each sample every second.
 *
 * Then for analysis, compares the mean of the uniform sample with the mean of
 * the data set to date and compares the mean of the exponentially-decaying
 * sample with the mean of the previous 5 minutes of values.
 */
object RecencyBiasExperiment {
  def main(args: Array[String]) {
    val expSample = new ExponentiallyDecayingSample(10, 0.015)
    val uniSample = new UniformSample(10)

    val output = new PrintWriter(new FileOutputStream("timings.csv"), true)
    output.println("t,exponential mean,expected exponential mean,uniform mean,expected uniform mean")

    for (t <- 1 to TimeUnit.HOURS.toSeconds(2).toInt) {
      expSample.update(t)
      uniSample.update(t)

      val expValues = expSample.getSnapshot.getValues.map {_.longValue}.sorted
      val uniValues = uniSample.getSnapshot.getValues.map {_.longValue}.sorted

      val expMean = expValues.sum / expValues.size.toDouble
      val expExpectedMean = ((t - 300).max(1) to t).sum / 300.0.min(t)
      val uniMean = uniValues.sum / uniValues.size.toDouble
      val uniExpectedMean = (1 to t).sum / t.toDouble

      println("=" * 80)
      println("t:   " + t)
      println("exp: " + expValues.mkString(", "))
      printf( "     mean:     %2.2f\n", expMean)
      printf( "     expected: %2.2f\n", expExpectedMean)
      println("uni: " + uniValues.mkString(", "))
      printf("     mean:     %2.2f\n", uniMean)
      printf("     expected: %2.2f\n", uniExpectedMean)

      output.println("%d,%2.2f,%2.2f,%2.2f,%2.2f".format(t, expMean, expExpectedMean, uniMean, uniExpectedMean))

      Thread.sleep(TimeUnit.SECONDS.toMillis(1))
    }

    output.close()
  }
}
