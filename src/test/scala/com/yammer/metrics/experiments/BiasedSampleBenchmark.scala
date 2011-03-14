package com.yammer.metrics.experiments

import com.yammer.metrics.Instrumented
import com.yammer.metrics.core.{Metrics, ExponentiallyDecayingSample}
import java.util.concurrent.{CountDownLatch, TimeUnit, Executors}

object BiasedSampleBenchmark extends Instrumented {
  val updateTimer = metrics.timer("update", durationUnit = TimeUnit.MICROSECONDS)

  def main(args: Array[String]) {
    Metrics.enableConsoleReporting(1, TimeUnit.SECONDS)

    val workerCount = 100
    val iterationCount = 1000000

    println("Warming up")
    locally { // warmup
      val sample = new ExponentiallyDecayingSample(1000, 0.015)
      for (i <- 1 to iterationCount) {
        sample.update(i)
      }
    }

    System.gc()
    System.gc()
    System.gc()
    System.gc()

    val sample = new ExponentiallyDecayingSample(1000, 0.015)
    val pool = Executors.newFixedThreadPool(workerCount)

    val latch = new CountDownLatch(workerCount)

    for (i <- 1 to workerCount) {
      pool.execute(new Runnable {
        def run = {
          latch.countDown()
          latch.await()
          for (j <- 1 to iterationCount) {
            updateTimer.time { sample.update(j) }
          }
        }
      })
    }
    
    pool.shutdown()
    pool.awaitTermination(10, TimeUnit.DAYS)
  }
}
