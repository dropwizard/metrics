package com.yammer.metrics.servlet.experiments

import org.eclipse.jetty.server.Server
import com.yammer.metrics.Instrumented
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import com.yammer.metrics.reporting.MetricsServlet
import com.yammer.metrics.jetty.{InstrumentedSelectChannelConnector, InstrumentedQueuedThreadPool, InstrumentedSocketConnector, InstrumentedHandler}

object TestServer extends Instrumented {
  val counter1 = metrics.counter("wah", "doody")
  val counter2 = metrics.counter("woo")
  val asplodingGauge = metrics.gauge[Int]("boo") {
    throw new RuntimeException("asplode!")
  }

  def main(args: Array[String]) {
    val server = new Server()
    val connector = new InstrumentedSelectChannelConnector(8080)
    server.addConnector(connector)

    val threadPool = new InstrumentedQueuedThreadPool()
    server.setThreadPool(threadPool)

    val context = new ServletContextHandler
    context.setContextPath("/initial")
    val holder = new ServletHolder(classOf[MetricsServlet])
    context.addServlet(holder, "/dingo/*")

    server.setHandler(new InstrumentedHandler(context))
    server.start()
    server.join()
  }
}
