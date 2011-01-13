Metrics
=======

*Capturing JVM- and application-level metrics. So you know what's going on.*

Requirements
------------

* Java SE 6
* Scala 2.8.1
* Jackson 1.7.0


How To Use
----------

**First**, specify Metrics as a dependency:

    val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
    val metrics = "com.yammer" %% "metrics" % "2.0.0-BETA7"

(Or whatever it takes for you to get Maven or Ivy happy.)

**Second**, instrument your classes:

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

Metrics comes with four types of metrics:

* **Gauges** are instantaneous readings of values (e.g., a queue depth).
* **Counters** are 64-bit integers which can be incremented or decremented.
* **Meters** are increment-only counters which keep track of the rate of events.
  They provide mean rates, plus exponentially-weighted moving averages which
  use the same formula that the UNIX 1-, 5-, and 15-minute load averages use.
* **Timers** record the duration as well as the rate of events. In addition to
  the rate information that meters provide, timers also provide the count,
  maximum, minimum, mean, standard deviation, median, 75th percentile, 95th
  percentile, 98th percentile, 99th percentile, and 99.9th percentile
  of timings. (They do so using a method called reservoir sampling which allows
  them to efficiently keep a small, statistically representative sample of all
  the measurements.)

Metrics also has support for health checks:

    Metrics.registerHealthCheck("database", new HealthCheck {
      def check = {
        if (Database.isConnected) {
          Result.healthy()
        } else {
          Result.unhealthy("Not connected to database")
        }
      }
    })

**Third**, start collecting your metrics.

All metrics are reported via JMX, which you can view using VisualVM or JConsole.

If you're simply running a benchmark, you can print registered metrics to 
standard error every 10s like this:

    Metrics.enableConsoleReporting(10, TimeUnit.SECONDS) // print to STDERR every 10s

If you're writing a Servlet-based web service, you can add `MetricsServlet` to
an internally-accessible context. It'll respond to the following URIs:
    
* `/metrics`: A JSON object of all registered metrics and a host of JVM metrics.
* `/ping`: A simple `text/plain` "pong" for load-balancers.
* `/healthcheck`: Runs through all registered `HealthCheck` instances and 
                  reports the results. Returns a `200 OK` if all succeeded, or a
                  `500 Internal Server Error` if any failed.
* `/threads`: A `text/plain` dump of all threads and their stack traces.


License
-------

Copyright (c) 2010 Coda Hale, Yammer.com

Published under The MIT License, see LICENSE