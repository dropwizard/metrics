.. _manual-core:

############
Metrics Core
############

.. highlight:: text

The central library for Metrics is ``metrics-core``, which provides some basic functionality:

* The five metric types: :ref:`man-core-gauges`, :ref:`man-core-counters`,
  :ref:`man-core-histograms`, :ref:`man-core-meters`, and :ref:`man-core-timers`.
* :ref:`man-core-healthchecks`
* Reporting metrics values via :ref:`JMX <man-core-reporters-jmx>`, the
  :ref:`console <man-core-reporters-console>`, and :ref:`CSV <man-core-reporters-csv>` files.

All metrics are created via either the ``Metrics`` class or a ``MetricsRegistry``. If your
application is running alongside other applications in a single JVM instance (e.g., multiple WARs
deployed to an application server), you should use per-application ``MetricsRegistry`` instances. If
your application is the sole occupant of the JVM instance (e.g., a Dropwizard_ application), feel
free to use the ``static`` factory methods on ``Metrics``.

.. _Dropwizard: http://dropwizard.codahale.com/

For this documentation, we'll assume you're using ``Metrics``, but the interfaces are much the same.

.. _man-core-names:

Metric Names
============

Each metric has a unique *metric name*, which consists of four pieces of information:

Group
  The top-level grouping of the metric. When a metric belongs to a class, this defaults to the
  class's *package name* (e.g., ``com.example.proj.auth``).
Type
  The second-level grouping of the metric. When a metric belongs to a class, this defaults to the
  class's *name* (e.g., ``SessionStore``).
Name
  A short name describing the metric's purpose (e.g., ``session-count``).
Scope
  An optional name describing the metric's scope. Useful for when you have multiple instances of a
  class.

The factory methods on ``Metrics`` and ``MetricsRegistry`` will accept either class/name,
class/name/scope, or ``MetricName`` instances with arbitrary inputs.


.. _man-core-gauges:

Gauges
======

A gauge is the simplest metric type. It just returns a *value*. If, for example, your application
has a value which is maintained by a third-party library, you can easily expose it by registering a
``Gauge`` instance which returns that value:

.. code-block:: java

    Metrics.newGauge(SessionStore.class, "cache-evictions", new Gauge<Integer>() {
        @Override
        public Integer value() {
            return cache.getEvictionsCount();
        }
    });

This will create a new gauge named ``com.example.proj.auth.SessionStore.cache-evictions`` which will
return the number of evictions from the cache.

.. _man-core-gauges-jmx:

JMX Gauges
----------

Given that many third-party library often expose metrics only via JMX, Metrics provides the
``JmxGauge`` class, which takes the object name of a JMX MBean and the name of an attribute and
produces a gauge implementation which returns the value of that attribute:

.. code-block:: java

    Metrics.newGauge(SessionStore.class, "cache-evictions",
                     new JmxGauge("net.sf.ehcache:type=Cache,scope=sessions,name=eviction-count", "Value"));

.. todo:: document RatioGauge

.. todo:: document PercentGauge

.. _man-core-counters:

Counters
========

A counter is a simple incrementing and decrementing 64-bit integer:

.. code-block:: java

    final Counter evictions = Metrics.newCounter(SessionStore.class, "cache-evictions");
    evictions.inc();
    evictions.inc(3);
    evictions.dec();
    evictions.dec(2);

All ``Counter`` metrics start out at 0.

.. _man-core-histograms:

Histograms
==========

A ``Histogram`` measures the distribution of values in a stream of data: e.g., the number of results
returned by a search:

.. code-block:: java

    final Histogram resultCounts = Metrics.newHistogram(ProductDAO.class, "result-counts");
    resultCounts.update(results.size());

``Histogram`` metrics allow you to measure not just easy things like the min, mean, max, and
standard deviation of values, but also quantiles__ like the median or 95th percentile.

.. __: http://en.wikipedia.org/wiki/Quantile

Traditionally, the way the median (or any other quantile) is calculated is to take the entire data
set, sort it, and take the value in the middle (or 1% from the end, for the 99th percentile). This
works for small data sets, or batch processing systems, but not for high-throughput, low-latency
services.

The solution for this is to sample the data as it goes through. By maintaining a small, manageable
sample which is statistically representative of the data stream as a whole, we can quickly and
easily calculate quantiles which are valid approximations of the actual quantiles. This technique is
called **reservoir sampling**.

Metrics provides two types of histograms: :ref:`uniform <man-core-histograms-uniform>`
and :ref:`biased <man-core-histograms-biased>`.

.. _man-core-histograms-uniform:

Uniform Histograms
------------------

A uniform histogram produces quantiles which are valid for the entirely of the histogram's lifetime.
It will return a median value, for example, which is the median of all the values the histogram has
ever been updated with. It does this by using an algorithm called `Vitter's R`__), which randomly
selects values for the sample with linearly-decreasing probability.

.. __: http://www.cs.umd.edu/~samir/498/vitter.pdf

Use a uniform histogram when you're interested in long-term measurements. Don't use one where you'd
want to know if the distribution of the underlying data stream has changed recently.

.. _man-core-histograms-biased:

Biased Histograms
-----------------

A biased histogram produces quantiles which are representative of (roughly) the last five minutes of
data. It does so by using a `forward-decaying priority sample`__ with an exponential weighting
towards newer data. Unlike the uniform histogram, a biased histogram represents **recent data**,
allowing you to know very quickly if the distribution of the data has changed.
:ref:`man-core-timers` use biased histograms.

.. __: http://www.research.att.com/people/Cormode_Graham/library/publications/CormodeShkapenyukSrivastavaXu09.pdf

.. _man-core-meters:

Meters
======

A meter measures the *rate* at which a set of events occur:

.. code-block:: java

    final Meter getRequests = Metrics.newMeter(WebProxy.class, "get-requests", "requests", TimeUnit.SECONDS);
    getRequests.mark();
    getRequests.mark(requests.size());

A meter requires two additional pieces of information besides the name: the **event type** and the
**rate unit**. The event type simply describes the type of events which the meter is measuring. In
the above case, the meter is measuring proxied requests, and so its event type is ``"requests"``.
The rate unit is the unit of time denominating the rate. In the above case, the meter is measuring
the number of requests in each second, and so its rate unit is ``SECONDS``. When combined, the meter
is measuring requests per second.

Meters measure the rate of the events in a few different ways. The *mean* rate is the average rate
of events. It's generally useful for trivia, but as it represents the total rate for your
application's entire lifetime (e.g., the total number of requests handled, divided by the number of
seconds the process has been running), it doesn't offer a sense of recency. Luckily, meters also
record three different *exponentially-weighted moving average* rates: the 1-, 5-, and 15-minute
moving averages.

.. hint::

    Just like the Unix load averages visible in ``uptime`` or ``top``.

.. _man-core-timers:

Timers
======

A timer is basically a :ref:`histogram <man-core-histograms>` of the duration of a type of event and
a :ref:`meter <man-core-meters>` of the rate of its occurrence.

.. code-block:: java

    final Timer timer = Metrics.newTimer(WebProxy.class, "get-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    final TimerContext context = timer.time();
    try {
        // handle request
    } finally {
        context.stop();
    }

A timer requires two additional pieces of information besides the name: the **duration unit** and
the **rate unit**. The duration unit is the unit of time in which the durations of events will be
measured. In the above example, the duration unit is ``MILLISECONDS``, meaning the timed event's
duration will be measured in milliseconds. The rate unit in the above example is ``SECONDS``,
meaning the rate of the timed event is measured in calls/sec.

.. note::

    Regardless of the display duration unit of a timer, elapsed time for its events is measured
    internally in nanoseconds, using Java's high-precision ``System.nanoTime()`` method.

.. _man-core-healthchecks:

Health Checks
=============

Metrics also provides you with a consistent, unified way of performing application health checks. A
health check is basically a small self-test which your application performs to verify that a
specific component or responsibility is performing correctly.

To create a health check, extend the ``HealthCheck`` class:

.. code-block:: java

    public class DatabaseHealthCheck extends HealthCheck {
        private final Database database;

        public DatabaseHealthCheck(Database database) {
            super("database");
            this.database = database;
        }

        @Override
        protected Result check() throws Exception {
            if (database.ping()) {
                return Result.healthy();
            }
            return Result.unhealthy("Can't ping database");
        }
    }

In this example, we've created a health check for a ``Database`` class on which our application
depends. Our fictitious ``Database`` class has a ``#ping()`` method, which executes a safe test
query (e.g., ``SELECT 1``). ``#ping()`` returns ``true`` if the query returns the expected result,
returns ``false`` if it returns something else, and throws an exception if things have gone
seriously wrong.

Our ``DatabaseHealthCheck``, then, takes a ``Database`` instance and in its ``#check()`` method,
attempts to ping the database. If it can, it returns a **healthy** result. If it can't, it returns
an **unhealthy** result.

.. note::

    Exceptions thrown inside a health check's ``#check()`` method are automatically caught and
    turned into unhealthy results with the full stack trace.

To register a health check, either use the ``HealthChecks`` singleton or a ``HealthCheckRegistry``
instance:

.. code-block:: java

    HealthChecks.register(new DatabaseHealthCheck(database));

You can also run the set of registered health checks:

.. code-block:: java

    for (Entry<String, Result> entry : HealthChecks.run().entrySet()) {
        if (entry.getValue().isHealthy()) {
            System.out.println(entry.getKey() + ": PASS");
        } else {
            System.out.println(entry.getKey() + ": FAIL");
        }
    }

.. _man-core-reporters:

Reporters
=========

Reporters are the way that your application exports all the measurements being made by its metrics.
``metrics-core`` comes with three ways of exporting your metrics:
:ref:`JMX <man-core-reporters-jmx>`, :ref:`console <man-core-reporters-console>`, and
:ref:`CSV <man-core-reporters-csv>`.

.. _man-core-reporters-jmx:

JMX
---

By default, Metrics always registers your metrics as JMX MBeans. To explore this you can use
VisualVM__ (which ships with most JDKs as ``jvisualvm``) with the VisualVM-MBeans plugins installed
or JConsole (which ships with most JDKs as ``jconsole``):

.. __: http://visualvm.java.net/

.. image:: ../metrics-visualvm.png
    :alt: Metrics exposed as JMX MBeans being viewed in VisualVM's MBeans browser

.. tip::

    If you double-click any of the metric properties, VisualVM will start graphing the data for that
    property. Sweet, eh?

Reporting via JMX is always enabled, but we don't recommend that you try to gather metrics from your
production environment. JMX's RPC API is fragile and bonkers. For development purposes and browsing,
though, it can be very useful.

.. _man-core-reporters-console:

Console
-------

For simple benchmarks, Metrics comes with ``ConsoleReporter``, which periodically reports all
registered metrics to the console:

.. code-block:: java

    ConsoleReporter.enable(1, TimeUnit.SECONDS);

.. _man-core-reporters-csv:

CSV
---

For more complex benchmarks, Metrics comes with ``CsvReporter``, which periodically appends to a set
of ``.csv`` files in a given directory:

.. code-block:: java

    CsvReporter.enable(new File("work/measurements"), 1, TimeUnit.SECONDS);

For each metric registered, a ``.csv`` file will be created, and every second its state will be
written to it as a new row.

.. _man-core-reporters-other:

Other Reporters
---------------

Metrics has other reporter implementations, too:

* :ref:`MetricsServlet <manual-servlet>` is a servlet which not only exposes your metrics as a JSON
  object, but it also runs your health checks, performs thread dumps, and exposes valuable JVM-level
  and OS-level information.
* :ref:`GangliaReporter <manual-ganglia>` allows you to constantly stream metrics data to your
  Ganglia servers.
* :ref:`GraphiteReporter <manual-graphite>` allows you to constantly stream metrics data to your
  Graphite servers.

