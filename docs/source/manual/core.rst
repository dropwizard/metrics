.. _manual-core:

############
Metrics Core
############

.. highlight:: text

The central library for Metrics is ``metrics-core``, which provides some basic functionality:

* Metric :ref:`registries <man-core-registries>`.
* The five metric types: :ref:`man-core-gauges`, :ref:`man-core-counters`,
  :ref:`man-core-histograms`, :ref:`man-core-meters`, and :ref:`man-core-timers`.
* Reporting metrics values via :ref:`JMX <man-core-reporters-jmx>`, the
  :ref:`console <man-core-reporters-console>`, :ref:`CSV <man-core-reporters-csv>` files, and
  :ref:`SLF4J loggers <man-core-reporters-slf4j>`.

.. _man-core-registries:

Metric Registries
=================

The starting point for Metrics is the ``MetricRegistry`` class, which is a collection of all the
metrics for your application (or a subset of your application).

Generally you only need one ``MetricRegistry`` instance per application, although you may choose
to use more if you want to organize your metrics in particular reporting groups.

Global named registries can also be shared through the static ``SharedMetricRegistries`` class. This
allows the same registry to be used in different sections of code without explicitly passing a ``MetricRegistry``
instance around.

Like all Metrics classes, ``SharedMetricRegistries`` is fully thread-safe.

.. _man-core-names:

Metric Names
============

Each metric is associated with a ``MetricRegistry``, and has a unique *name* within that registry. This is a simple
dotted name, like ``com.example.Queue.size``. This flexibility allows you to encode a wide variety of
context directly into a metric's name. If you have two instances of ``com.example.Queue``, you can give
them more specific: ``com.example.Queue.requests.size`` vs. ``com.example.Queue.responses.size``, for example.

``MetricRegistry`` has a set of static helper methods for easily creating names:

.. code-block:: java

    MetricRegistry.name(Queue.class, "requests", "size")
    MetricRegistry.name(Queue.class, "responses", "size")

These methods will also elide any ``null`` values, allowing for easy optional scopes.

.. _man-core-gauges:

Gauges
======

A gauge is the simplest metric type. It just returns a *value*. If, for example, your application
has a value which is maintained by a third-party library, you can easily expose it by registering a
``Gauge`` instance which returns that value:

.. code-block:: java

    registry.register(name(SessionStore.class, "cache-evictions"), new Gauge<Integer>() {
        @Override
        public Integer getValue() {
            return cache.getEvictionsCount();
        }
    });

This will create a new gauge named ``com.example.proj.auth.SessionStore.cache-evictions`` which will
return the number of evictions from the cache.

.. _man-core-gauges-jmx:

JMX Gauges
----------

Given that many third-party libraries often expose metrics only via JMX, Metrics provides the
``JmxAttributeGauge`` class, which takes the object name of a JMX MBean and the name of an attribute
and produces a gauge implementation which returns the value of that attribute:

.. code-block:: java

    registry.register(name(SessionStore.class, "cache-evictions"),
                     new JmxAttributeGauge("net.sf.ehcache:type=Cache,scope=sessions,name=eviction-count", "Value"));

.. _man-core-gauges-ratio:

Ratio Gauges
------------

A ratio gauge is a simple way to create a gauge which is the ratio between two numbers:

.. code-block:: java

    public class CacheHitRatio extends RatioGauge {
        private final Meter hits;
        private final Timer calls;

        public CacheHitRatio(Meter hits, Timer calls) {
            this.hits = hits;
            this.calls = calls;
        }

        @Override
        public Ratio getRatio() {
            return Ratio.of(hits.getOneMinuteRate(),
                            calls.getOneMinuteRate());
        }
    }

This gauge returns the ratio of cache hits to misses using a meter and a timer.

.. _man-core-gauges-cached:

Cached Gauges
-------------

A cached gauge allows for a more efficient reporting of values which are expensive
to calculate. The value is cached for the period specified in the constructor. The "getValue()"
method called by the client only returns the cached value. The protected "loadValue()" method
is only called internally to reload the cache value.

.. code-block:: java

    registry.register(name(Cache.class, cache.getName(), "size"),
                      new CachedGauge<Long>(10, TimeUnit.MINUTES) {
                          @Override
                          protected Long loadValue() {
                              // assume this does something which takes a long time
                              return cache.getSize();
                          }
                      });

.. _man-core-gauges-derivative:

Derivative Gauges
-----------------

A derivative gauge allows you to derive values from other gauges' values:

.. code-block:: java

    public class CacheSizeGauge extends DerivativeGauge<CacheStats, Long> {
        public CacheSizeGauge(Gauge<CacheStats> statsGauge) {
            super(statsGauge);
        }

        @Override
        protected Long transform(CacheStats stats) {
            return stats.getSize();
        }
    }

.. _man-core-counters:

Counters
========

A counter is a simple incrementing and decrementing 64-bit integer:

.. code-block:: java

    final Counter evictions = registry.counter(name(SessionStore.class, "cache-evictions"));
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

    final Histogram resultCounts = registry.histogram(name(ProductDAO.class, "result-counts");
    resultCounts.update(results.size());

``Histogram`` metrics allow you to measure not just easy things like the min, mean, max, and
standard deviation of values, but also quantiles__ like the median or 95th percentile.

.. __: http://en.wikipedia.org/wiki/Quantile

Traditionally, the way the median (or any other quantile) is calculated is to take the entire data
set, sort it, and take the value in the middle (or 1% from the end, for the 99th percentile). This
works for small data sets, or batch processing systems, but not for high-throughput, low-latency
services.

The solution for this is to sample the data as it goes through. By maintaining a small, manageable
reservoir which is statistically representative of the data stream as a whole, we can quickly and
easily calculate quantiles which are valid approximations of the actual quantiles. This technique is
called **reservoir sampling**.

Metrics provides a number of different ``Reservoir`` implementations, each of which is useful.

.. _man-core-histograms-uniform:

Uniform Reservoirs
------------------

A histogram with a uniform reservoir produces quantiles which are valid for the entirely of the
histogram's lifetime. It will return a median value, for example, which is the median of all the
values the histogram has ever been updated with. It does this by using an algorithm called
`Vitter's R`__), which randomly selects values for the reservoir with linearly-decreasing
probability.

.. __: http://www.cs.umd.edu/~samir/498/vitter.pdf

Use a uniform histogram when you're interested in long-term measurements. Don't use one where you'd
want to know if the distribution of the underlying data stream has changed recently.

.. _man-core-histograms-exponential:

Exponentially Decaying Reservoirs
---------------------------------

A histogram with an exponentially decaying reservoir produces quantiles which are representative of
(roughly) the last five minutes of data. It does so by using a
`forward-decaying priority reservoir`__ with an exponential weighting towards newer data. Unlike the
uniform reservoir, an exponentially decaying reservoir represents **recent data**, allowing you to
know very quickly if the distribution of the data has changed. :ref:`man-core-timers` use histograms
with exponentially decaying reservoirs by default.

.. __: http://dimacs.rutgers.edu/~graham/pubs/papers/fwddecay.pdf

.. _man-core-histograms-sliding:

Sliding Window Reservoirs
-------------------------

A histogram with a sliding window reservoir produces quantiles which are representative of the past
``N`` measurements.

.. _man-core-histograms-sliding-time:

Sliding Time Window Reservoirs
------------------------------

A histogram with a sliding time window reservoir produces quantiles which are strictly
representative of the past ``N`` seconds (or other time period).

.. warning::

    While ``SlidingTimeWindowReservoir`` is easier to understand than
    ``ExponentiallyDecayingReservoir``, it is not bounded in size, so using it to sample a
    high-frequency process can require a significant amount of memory. Because it records every
    measurement, it's also the slowest reservoir type.

.. hint::

    Try to use our new optimised version of ``SlidingTimeWindowReservoir`` called ``SlidingTimeWindowArrayReservoir``.
    It brings much lower memory overhead. Also it's allocation/free patterns are different,
    so GC overhead is 60x-80x lower then ``SlidingTimeWindowReservoir``. Now ``SlidingTimeWindowArrayReservoir`` is
    comparable with ``ExponentiallyDecayingReservoir`` in terms GC overhead and performance.
    As for required memory, ``SlidingTimeWindowArrayReservoir`` takes ~128 bits per stored measurement and you can simply
    calculate required amount of heap.

    Example: 10K measurements / sec with reservoir storing time of 1 minute will take
    10000 * 60 * 128 / 8 = 9600000 bytes ~ 9 megabytes

.. _man-core-meters:

Meters
======

A meter measures the *rate* at which a set of events occur:

.. code-block:: java

    final Meter getRequests = registry.meter(name(WebProxy.class, "get-requests", "requests"));
    getRequests.mark();
    getRequests.mark(requests.size());

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

    final Timer timer = registry.timer(name(WebProxy.class, "get-requests"));

    final Timer.Context context = timer.time();
    try {
        // handle request
    } finally {
        context.stop();
    }

.. note::

    Elapsed times for it events are measured internally in nanoseconds, using Java's high-precision
    ``System.nanoTime()`` method. Its precision and accuracy vary depending on operating system and
    hardware.

.. _man-core-sets:

Metric Sets
===========

Metrics can also be grouped together into reusable metric sets using the ``MetricSet`` interface.
This allows library authors to provide a single entry point for the instrumentation of a wide
variety of functionality.

.. _man-core-reporters:

Reporters
=========

Reporters are the way that your application exports all the measurements being made by its metrics.
``metrics-core`` comes with four ways of exporting your metrics:
:ref:`JMX <man-core-reporters-jmx>`, :ref:`console <man-core-reporters-console>`,
:ref:`SLF4J <man-core-reporters-slf4j>`, and :ref:`CSV <man-core-reporters-csv>`.

.. _man-core-reporters-jmx:

JMX
---

With ``JmxReporter``, you can expose your metrics as JMX MBeans. To explore this you can use
VisualVM__ (which ships with most JDKs as ``jvisualvm``) with the VisualVM-MBeans plugins installed
or JConsole (which ships with most JDKs as ``jconsole``):

.. __: http://visualvm.java.net/

.. image:: ../metrics-visualvm.png
    :alt: Metrics exposed as JMX MBeans being viewed in VisualVM's MBeans browser

.. tip::

    If you double-click any of the metric properties, VisualVM will start graphing the data for that
    property. Sweet, eh?

.. warning::

    We don't recommend that you try to gather metrics from your production environment. JMX's RPC
    API is fragile and bonkers. For development purposes and browsing, though, it can be very
    useful.

To report metrics via JMX:

.. code-block:: java

    final JmxReporter reporter = JmxReporter.forRegistry(registry).build();
    reporter.start();

.. _man-core-reporters-console:

Console
-------

For simple benchmarks, Metrics comes with ``ConsoleReporter``, which periodically reports all
registered metrics to the console:

.. code-block:: java

    final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
                                                    .convertRatesTo(TimeUnit.SECONDS)
                                                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                    .build();
    reporter.start(1, TimeUnit.MINUTES);

.. _man-core-reporters-csv:

CSV
---

For more complex benchmarks, Metrics comes with ``CsvReporter``, which periodically appends to a set
of ``.csv`` files in a given directory:

.. code-block:: java

    final CsvReporter reporter = CsvReporter.forRegistry(registry)
                                            .formatFor(Locale.US)
                                            .convertRatesTo(TimeUnit.SECONDS)
                                            .convertDurationsTo(TimeUnit.MILLISECONDS)
                                            .build(new File("~/projects/data/"));
    reporter.start(1, TimeUnit.SECONDS);

For each metric registered, a ``.csv`` file will be created, and every second its state will be
written to it as a new row.

.. _man-core-reporters-slf4j:

SLF4J
-----

It's also possible to log metrics to an SLF4J logger:

.. code-block:: java

    final Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                                                .outputTo(LoggerFactory.getLogger("com.example.metrics"))
                                                .convertRatesTo(TimeUnit.SECONDS)
                                                .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                .build();
    reporter.start(1, TimeUnit.MINUTES);

.. _man-core-reporters-other:

Other Reporters
---------------

Metrics has other reporter implementations, too:

* :ref:`MetricsServlet <manual-servlets>` is a servlet which not only exposes your metrics as a JSON
  object, but it also runs your health checks, performs thread dumps, and exposes valuable JVM-level
  and OS-level information.
* :ref:`GraphiteReporter <manual-graphite>` allows you to constantly stream metrics data to your
  Graphite servers.

