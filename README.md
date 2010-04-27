Metrics
=======

*Capturing and exposing application metrics via JMX. For funsies.*

Requirements
------------

* Java SE 6
* Scala 2.8 Beta1


How To Use
----------

**First**, specify Metrics as a dependency:

    val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
    val metrics = "com.yammer" %% "metrics" % "1.0.4" withSources()

(Or whatever it takes for you to get Maven or Ivy happy.)

**Second**, instrument your classes:

    import com.yammer.metrics.{Counter, Meter, Timer}
    
    class ThingFinder {
      private val resultsMeter = new Meter
      private val dbTimer = new Timer
      
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

(You also might like `LoadMeter`, a meter class which provides 1-minute,
5-minute, and 15-minute moving weighted averages, much like the load values in
`top`. It's generally a more useful metric than `Meter`'s averaged rate.)

**Third**, expose these metrics via JMX:
    
    import java.util.concurrency.TimeUnit
    import com.yammer.jmx.JmxManaged
    
    class ThingFinder extends JmxManaged {
      private val resultsMeter = new Meter
      private val dbTimer = new Timer
      
      enableJMX("our thing processor") { jmx =>
        // exposes the total count of results, plus the results/sec rate
        jmx.addMeter("results", resultsMeter, TimeUnit.SECONDS)
        
        // exposes the count, max, min, mean, stddev, and 99.9th percentile of
        // query timings in milliseconds
        jmx.addTimer("database-query", dbTimer, TimeUnit.MILLISECONDS)
      }
      
      def findThings() = {
        // etc.
      }
    }


License
-------

Copyright (c) 2010 Coda Hale, Yammer.com
Published under The MIT License, see LICENSE