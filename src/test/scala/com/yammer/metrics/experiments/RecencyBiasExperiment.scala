package com.yammer.metrics.experiments

import collection.JavaConversions._
import java.util.concurrent.TimeUnit
import com.yammer.metrics.core.{UniformSample, ExponentiallyDecayingSample}
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
  /**
   * For analysis.
   */
  val RScript = """
#!/usr/bin/env Rscript --vanilla
if (!library("getopt", character.only = TRUE, logical.return = TRUE)) {
  install.packages("getopt", repos = "http://lib.stat.cmu.edu/R/CRAN")
}
require("getopt")

# Setup parameters for the script
params = matrix(c(
  'help',    'h', 0, "logical",
  'input',   'i', 2, "character"
  ), ncol=4, byrow=TRUE)

# Parse the parameters
opt = getopt(params)

data <- read.csv(file=opt$input,head=TRUE,sep=",")
plot(data$t, data$exponential.mean, "l", xlab="Time", ylab="Mean", col="tomato")
lines(data$expected.exponential.mean, col="tomato4")
lines(data$uniform.mean, col="violetred")
lines(data$expected.uniform.mean, col="violetred4")
"""

  def main(args: Array[String]) {
    val expSample = new ExponentiallyDecayingSample(10, 0.015)
    val uniSample = new UniformSample(10)

    val output = new PrintWriter(new FileOutputStream("timings.csv"), true)
    output.println("t,exponential mean,expected exponential mean,uniform mean,expected uniform mean")

    for (t <- 1 to TimeUnit.HOURS.toSeconds(2).toInt) {
      expSample.update(t)
      uniSample.update(t)

      val expValues = expSample.values.map {_.longValue}.sorted
      val uniValues = uniSample.values.map {_.longValue}.sorted

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
