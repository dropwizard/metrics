Getting Started With Metrics
============================

This tutorial will walk you through the basic steps involved in using Metrics
with your project.

.. toctree::
   :maxdepth: 1

Add Metrics as a dependency
---------------------------

The first step to using Metrics in your project is to add the Metrics JAR to
your project's classpath.


Using Maven
^^^^^^^^^^^

If you're using Maven, you can add my Maven repository to your ``pom.xml`` file
like this:

.. code-block:: xml

    <repositories>
      <repository>
        <id>repo.codahale.com</id>
        <url>http://repo.codahale.com</url>
      </repository>
    </repositories>

And then the Metrics library like this:

.. code-block:: xml

    <dependencies>
      <dependency>
        <groupId>com.yammer.metrics</groupId>
        <artifactId>metrics-core_2.8.1</artifactId>
        <version>2.0.0-BETA12</version>
      </dependency>
    </dependencies>

The next time you use Maven, it should download the Metrics JAR.


Using Simple Build Tool
^^^^^^^^^^^^^^^^^^^^^^^

To add Metrics as a dependency in your
`Simple Build Tool <http://simple-build-tool.googlecode.com>`_ project, add our
Maven repository to your project class:

.. code-block:: scala

    val codaRepo = "Coda Hale's Repo" at "http://repo.codahale.com"

Then add ``metrics-core`` as a dependency:

.. code-block:: scala

    val metrics = "com.yammer.metrics" %% "metrics-core" % "2.0.0-BETA12"


Finally, run ``sbt update``. You should be ready to go.


Raw-doggin' It
^^^^^^^^^^^^^^

If you're not using dependency management of any kind, just go ahead and copy
the JAR file out of
`the Maven repo <http://repo.codahale.com/com/yammer/metrics/>`_ for the
version you'd like.

(That said, we recommend you use a dependency mangement tool. They make life
easier.)


Instrument Your Code
--------------------

If you're using the optional Scala façade, make sure the class you're
instrumenting extends Metrics' ``Instrumented`` trait. (If you're just using the
Java interface this isn't necessary.)

You should then go through your code, looking for various behaviors which could
affect your code's business value. When you find something, add one of these
five different types of metrics:

* :ref:`gauges`
* :ref:`counters`
* :ref:`meters`
* :ref:`histograms`
* :ref:`timers`


Adding A Gauge
^^^^^^^^^^^^^^

A gauge is an instantaneous measurement of a value. For example, we may want to
measure the number of pending jobs in a queue.

Java
****

.. code-block:: java

    Metrics.newGauge(QueueManager.class, "pending-jobs", new GaugeMetric<Integer>() {
        @Override
        public Integer value() {
            return queue.size();
        }
    });

Scala
*****

.. code-block:: java

    metrics.gauge("pending-jobs") { queue.size }


Every time this gauge is measured, it will return the number of jobs in the
queue.

**N.B.:** For most queue and queue-like structures, you won't want to simply
return ``queue.size()``. Most of ``java.util`` and ``java.util.concurrent``'s
implementations of ``#size()`` are ``O(n)``, which means your gauge will be
slow.

:ref:`Learn more about gauges. <gauges>`


Adding A Counter
^^^^^^^^^^^^^^^^

A counter is just a gauge for an ``AtomicLong`` instance. You can increment or
decrement its value. For example, we may want a more efficient way of measuring
the pending job in a queue:

Java
****


.. code-block:: java

    private final CounterMetric pendingJobs = Metrics.newCounter(QueueManager.class, "pending-jobs");
    
    public void addJob(Job job) {
        pendingJobs.inc();
        queue.offer(job);
    }
    
    public Job takeJob() {
        pendingJobs.dec();
        return queue.take();
    }


Scala
*****

.. code-block:: scala

    private val pendingJobs = metrics.counter("pending-jobs")
    
    def addJob(job: Job) {
      pendingJobs += 1
      queue.offer(job)
    }
    
    def takeJob() = {
      pendingJobs -= 1
      queue.take()
    }

Every time this counter is measured, it will return the number of jobs in the
queue.

:ref:`Learn more about counters. <counters>`


Adding A Meter
^^^^^^^^^^^^^^

A meter measures the rate of events over time (e.g., "requests per second").
In addition to the mean rate, meters also track 1-, 5-, and 15-minute moving
averages.

Java
****

.. code-block:: java

    private final MeterMetric requests = Metrics.newMeter(RequestHandler.class, "requests", "requests", TimeUnit.SECONDS);
    
    public void handleRequest(Request request, Response response) {
        requests.mark();
        // etc
    }

Scala
*****

.. code-block:: scala

    private val requests = metrics.meter("requests", "requests")
    
    def handleRequest(request: Request, response: Response) {
      requests.mark()
      // etc
    }

This meter will measure the rate of requests in requests per second.

:ref:`Learn more about meters. <meters>`


Adding A Histogram
^^^^^^^^^^^^^^^^^^

A histogram measures the statistical distribution of values in a stream of data.
In addition to minimum, maximum, mean, etc., it also measures median, 75th,
90th, 95th, 98th, 99th, and 99.9th percentiles.

Java
****

.. code-block:: java

    private final HistogramMetric responseSizes = Metrics.newHistogram(RequestHandler.class, "response-sizes");
    
    public void handleRequest(Request request, Response response) {
        // etc
        responseSizes.update(response.getContent().length);
    }


Scala
*****

.. code-block:: scala

    private val responseSizes = metrics.histogram("response-sizes")
    
    def handleRequest(request: Request, response: Response) {
      // etc
      responseSizes += response.getContent.length
    }


This histogram will measure the size of responses in bytes.

:ref:`Learn more about histograms. <histograms>`


Adding A Timer
^^^^^^^^^^^^^^

A timer measures both the rate that a particular piece of code is called and the
distribution of its latency.

Java
****

.. code-block:: java

    private final TimerMetric responses = Metrics.newTimer(RequestHandler.class, "responses", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    
    public String handleRequest(Request request, Response response) {
        return responses.time(new Callable<String> () {
            @Override
            public String call() {
                // etc;
                return "OK";
            }
        });
    }

    
Scala
*****

.. code-block:: scala

    private val responses = metrics.timer("responses")
    
    def handleRequest(request: Request, response: Response) = {
      responses.time {
        // etc.
        "OK"
      }
    }


This timer will measure the amount of time it takes to process each request in
milliseconds and provide a rate of requests in requests per second.

:ref:`Learn more about timers. <timers>`


Adding A Health Check
^^^^^^^^^^^^^^^^^^^^^

Metrics also has the ability to centralize your service's health checks. First,
implement a ``HealthCheck`` instance:


Java
****

.. code-block:: java

    import com.yammer.metrics.core.HealthCheck.Result;

    public class DatabaseHealthCheck extends HealthCheck {
        private final Database database;
  
        public DatabaseHealthCheck(Database database) {
            this.database = database;
        }
    
        @Override
        public String name() {
            return "database";
        }
    
        @Override
        public Result check() throws Exception {
            if (database.isConnected()) {
                return Result.healthy();
            } else {
                return Result.unhealthy("Cannot connect to " + database.getUrl());
            }
        }
    }

Then register an instance of it with Metrics:

.. code-block:: java

    HealthChecks.register(new DatabaseHealthCheck(database));

To run all of the registered health checks:

.. code-block:: java

    final Map<String, Result> results = HealthChecks.runHealthChecks();
    for (Entry<String, Result> entry : results.entrySet()) {
        if (entry.getValue().isHealthy()) {
            System.out.println(entry.getKey() + " is healthy");
        } else {
            System.err.println(entry.getKey() + " is UNHEALTHY: " + entry.getValue().getMessage());
            final Throwable e = entry.getValue().getError();
            if (e != null) {
                e.printStackTrace();
            }
        }
    }

For Scala the code is pretty much the same but much more concise.

Metrics comes with a pre-built health check: `DeadlockHealthCheck`, which uses
Java 1.6's built-in thread deadlock detection to determine if any threads are
deadlocked.

:ref:`Learn more about health checks. <healthchecks>`


Exposing Metrics via JMX
^^^^^^^^^^^^^^^^^^^^^^^^

.. todo: document JMX in tutorial


Exposing Metrics via HTTP+JSON
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. todo: document servlet in tutorial

