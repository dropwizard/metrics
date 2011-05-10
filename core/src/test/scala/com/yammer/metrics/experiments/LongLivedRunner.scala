package com.yammer.metrics.experiments

import com.yammer.metrics.{Metrics, Instrumented}
import java.util.concurrent.TimeUnit

object LongLivedRunner extends Instrumented {
  val counters = Seq("one", "two").map { s => s -> metrics.counter("counter", s) }.toMap

  def main(args: Array[String]) {
    Metrics.enableConsoleReporting(1, TimeUnit.SECONDS)
    
    val thread = new Thread {
      override def run() {
        while (true) {
          counters("one") += 1
          counters("two") += 2
        }
        Thread.sleep(100)
      }
    }
    thread.setDaemon(true)
    thread.start()

    println("Hit return to quit")
    readLine()
  }
}
