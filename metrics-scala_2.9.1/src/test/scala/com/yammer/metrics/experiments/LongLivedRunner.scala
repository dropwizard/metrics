package com.yammer.metrics.experiments

import com.yammer.metrics.scala.Instrumented
import java.util.concurrent.TimeUnit
import com.yammer.metrics.reporting.ConsoleReporter

object LongLivedRunner extends Instrumented {
  val counters = Seq("one", "two").map { s => s -> metrics.counter("counter", s) }.toMap

  def main(args: Array[String]) {
    ConsoleReporter.enable(1, TimeUnit.SECONDS)
    
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
