Metrics
=======

*Capturing JVM- and application-level metrics. So you know what's going on.*

Requirements
------------

* Java SE 6
* Scala 2.8.1 or 2.9.0.RC2 or 2.9.0.RC3.
* Guice 3.0 (for `metrics-guice`)
* Servlet API 2.5 (for `metrics-servlet`)
* Jackson 1.7.6 (for `metrics-servlet`)
* Jetty 7.4.0.v20110414 (for `metrics-jetty`)
* Log4J 1.2.16 (for `metrics-log4j`)
* Logback 0.9.28 (for `metrics-logback`)
* Ehcache 2.4.2 (for `metrics-ehcache`)


How To Use
----------

**First**, specify Metrics as a dependency:

```scala
val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
val metrics = "com.yammer.metrics" %% "metrics-core" % "2.0.0-BETA12"
```

(Or whatever it takes for you to get Maven or Ivy happy.)

**Second**, instrument your classes:

```scala
import java.util.concurrent.TimeUnit
import com.yammer.metrics.Instrumented

class ThingFinder extends Instrumented {
  // measure the # of records per second returned
  private val resultsMeter = metrics.meter("results", "records", TimeUnit.SECONDS)
  // measure the # of milliseconds each query takes and the number of
  // queries per second being performed
  private val dbTimer = metrics.timer("database", TimeUnit.MILLISECONDS, TimeUnit.SECONDS)

  def findThings() = {
    val results = dbTimer.time {
      // perform an action which gets timed
      Database.query("WHOO")
    }

    // calculate the rate of new things found
    resultsMeter.mark(results.size)

    // etc.
  }
}
```

If you're using Guice and `metrics-guice`, you can instrument your
Guice-provided classes via the `@Timed` and `@Metered` annotations in
conjunction with `InstrumentationModule`:

```scala
val injector = Guice.createInjector(new InstrumentationModule, ...)

class Database {
  @Timed
  def query(q: String): Seq[Result] = {
    // ...
  }

  @Metered
  def things() {
    // ...
  }
}
```

This will add a timer with the annotated method's name which records the
duration of method invocation or a meter which records the rate of invocation.

Metrics comes with five types of metrics:

* **Gauges** are instantaneous readings of values (e.g., a queue depth).
* **Counters** are 64-bit integers which can be incremented or decremented.
* **Meters** are increment-only counters which keep track of the rate of events.
  They provide mean rates, plus exponentially-weighted moving averages which
  use the same formula that the UNIX 1-, 5-, and 15-minute load averages use.
* **Histograms** capture distribution measurements about a metric: the count,
  maximum, minimum, mean, standard deviation, median, 75th percentile, 95th
  percentile, 98th percentile, 99th percentile, and 99.9th percentile of the
  recorded values. (They do so using a method called reservoir sampling which
  allows them to efficiently keep a small, statistically representative sample
  of all the measurements.)
* **Timers** record the duration as well as the rate of events. In addition to
  the rate information that meters provide, timers also provide the same metrics
  as histograms about the recorded durations. (The samples that timers keep in
  order to calculate percentiles and such are biased towards more recent data,
  since you probably care more about how your application is doing *now* as
  opposed to how it's done historically.)

Metrics also has support for health checks:

```scala
HealthChecks.register("database", new HealthCheck {
  def check = {
    if (Database.isConnected) {
      Result.healthy()
    } else {
      Result.unhealthy("Not connected to database")
    }
  }
})
```

**Third**, start collecting your metrics.

All metrics are reported via JMX, which you can view using VisualVM or JConsole.

If you're simply running a benchmark, you can print registered metrics to 
standard error every 10s like this:

```scala
ConsoleReporter.enable(10, TimeUnit.SECONDS) // print to STDERR every 10s
```

If you're writing a Servlet-based web service, you can add `MetricsServlet` from
the `metrics-servlet` subproject to an internally-accessible context. It'll
respond to the following URIs:
    
* `/metrics`: A JSON object of all registered metrics and a host of JVM metrics.
* `/ping`: A simple `text/plain` "pong" for load-balancers.
* `/healthcheck`: Runs through all registered `HealthCheck` instances and 
                  reports the results. Returns a `200 OK` if all succeeded, or a
                  `500 Internal Server Error` if any failed.
* `/threads`: A `text/plain` dump of all threads and their stack traces.

The URIs of these resources can be configured by passing the servlet the
`init-param`s `"metrics-uri"`, `"ping-uri"`, `"healthcheck-uri"`, and
`"threads-uri"`, or by passing these values to the servlet's constructor
(if you happen to be wiring your servlets by code).

If you use [Graphite](http://graphite.wikidot.com/), you can use the
`GraphiteReporter` class from the `metrics-graphite` library to have your
application report directly to the server:

```scala
GraphiteReporter.enable(1, TimeUnit.MINUTES, "graphite.example.com", 8080)
```

Optionally, you can provide a prefix to prepend to all metric names sent
to Graphite:

```scala
GraphiteReporter.enable(1, TimeUnit.MINUTES, "graphite.example.com", 8080, "my.host.name")
```

License
-------

Copyright (c) 2010-2011 Coda Hale, Yammer.com

Published under The MIT License, see LICENSE
