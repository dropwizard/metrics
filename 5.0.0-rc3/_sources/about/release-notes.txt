.. _release-notes:

#############
Release Notes
#############


.. _rel-4.0.0:

v4.0.0: Dec 24 2017
===================

* Compiled and targeted JDK8
* Support for running under JDK9 `#1236 <https://github.com/dropwizard/metrics/pull/1236>`_
* Move JMX reporting to the ``metrics-jmx`` module
* Add Bill of Materials for Metrics #1239 `#1239 <https://github.com/dropwizard/metrics/pull/1239>`_
* Used Java 8 Time API for data formatting
* Removed unnecessary reflection hacks for ``HealthCheckRegistry``
* Removed internal ``LongAdder``
* Removed internal ``ThreadLocalRandom``
* Optimized generating random numbers
* ``Timer.Context`` now implements ``AutoCloseable``
* Upgrade Jetty integration to Jetty 9.4
* Support tracking Jersey filters in Jersey resources `#1118 <https://github.com/dropwizard/metrics/pull/1239>`_
* Add ``ResponseMetered`` annotation for Jersey resources `#1186 <https://github.com/dropwizard/metrics/pull/1186>`_
* Add a method for timing non-throwing functions. `#1224 <https://github.com/dropwizard/metrics/pull/1224>`_
* Unnecessary clear operation for ChunkedAssociativeArray `#1211 <https://github.com/dropwizard/metrics/pull/1211>`_
* Add some common metric filters `#1210 <https://github.com/dropwizard/metrics/pull/1210>`_
* Add possibility to subclass Timer.Context `#1226 <https://github.com/dropwizard/metrics/pull/1226>`_

.. _rel-3.2.6:

v3.2.6: Dec 24 2017
===================

* Jetty9: unhandled response should be counted as 404 and not 200 `#1232 <https://github.com/dropwizard/metrics/pull/1232>`_
* Prevent NaN values when calculating mean `#1230 <https://github.com/dropwizard/metrics/pull/1230>`_
* Avoid NaN values in WeightedSnapshot `#1233 <https://github.com/dropwizard/metrics/pull/1233>`_

.. _rel-3.2.5:

v3.2.5: Sep 15 2017
===================

* [InstrumentedScheduledExecutorService] Fix the scheduledFixedDelay to call the correct method `#1192 <https://github.com/dropwizard/metrics/pull/1192>`_

.. _rel-3.2.4:

v3.2.4: Aug 24 2017
===================

* Fix GraphiteReporter rate reporting `#1167 <https://github.com/dropwizard/metrics/pull/1167>`_
* Remove non Jdk6 compatible letter from date pattern `#1163 <https://github.com/dropwizard/metrics/pull/1163>`_
* Fix uncaught CancellationException when stopping reporter `#1170 <https://github.com/dropwizard/metrics/pull/1170>`_

.. _rel-3.2.3:

v3.2.3: Jun 28 2017
===================

* Improve ``ScheduledReporter`` ``convertDurations`` precision `#1115 <https://github.com/dropwizard/metrics/pull/1115>`_
* Suppress all kinds of Throwables raised by ``report()`` `#1128 <https://github.com/dropwizard/metrics/pull/1128>`_
* ``ExponentiallyDecayingReservoir`` was giving incorrect values in the snapshot if the inactive period was too long `#1135 <https://github.com/dropwizard/metrics/pull/1135>`_
* Ability to get default metrics registry without an exception `#1140 <https://github.com/dropwizard/metrics/pull/1140>`_
* Ability to get default health check registry without an exception `#1152 <https://github.com/dropwizard/metrics/pull/1152>`_
* ``SlidingTimeWindowArrayReservoir`` as a fast alternative of ``SlidingTimeWindowReservoir`` `#1139 <https://github.com/dropwizard/metrics/pull/1139>`_
* Avoid a NPE in toString of ``HealthCheck.Result`` `#1141 <https://github.com/dropwizard/metrics/pull/1141>`_

.. _rel-3.1.5:

v3.1.5: Jun 2 2017
===================

* More robust lookup of ``ThreadLocal`` and ``LongAdder`` on JDK6 (e.g. WebLogic) `#1136 <https://github.com/dropwizard/metrics/pull/1136>`_

.. _rel-3.2.2:

v3.2.2: Mar 20 2017
===================

* Fix creating a uniform snapshot from a collection `#1111 <https://github.com/dropwizard/metrics/pull/1111>`_
* Register metrics defined at Resource level `#1105 <https://github.com/dropwizard/metrics/pull/1105>`_

.. _rel-3.2.1:

v3.2.1: Mar 10 2017
===================

* Support for shutting down the health check registry. `#1084 <https://github.com/dropwizard/metrics/pull/1084>`_
* Added support for the default shared health check registry name #1095 `#1095 <https://github.com/dropwizard/metrics/pull/1095>`_
* SharedMetricRegistries are now thread-safe. `#1094 <https://github.com/dropwizard/metrics/pull/1095>`_
* The size of the snapshot of a histogram is reported via JMX. `#1102 <https://github.com/dropwizard/metrics/pull/1102>`_
* Don't ignore the counter attribute for reporters. `#1090 <https://github.com/dropwizard/metrics/pull/1090>`_
* Added support for disabling attributes in ConsoleReporter. `#1092 <https://github.com/dropwizard/metrics/pull/1092>`_
* Rollbacked GraphiteSanitize to replacing whitespaces. `#1099 <https://github.com/dropwizard/metrics/pull/1099>`_

.. _rel-3.1.4:

v3.1.4: Mar 10 2017
===================

* Fix accidentally broken Graphite UDP reporter `#1100 <https://github.com/dropwizard/metrics/pull/1100>`_

.. _rel-3.2.0:

v3.2.0: Feb 24 2017
===================

* `GraphiteReporter` opens a new TCP connection when sending metrics instead of maintaining a persisted connection. `#1047 <https://github.com/dropwizard/metrics/pull/1047>`_
* `GraphiteReporter` retries DNS lookups in case of a lookup failure. `#1064 <https://github.com/dropwizard/metrics/pull/1064>`_
* `ScheduledReporter` suppresses all kind of exceptions raised by the `report` method. `#1049 <https://github.com/dropwizard/metrics/pull/1049>`_
* JDK's `ThreadLocalRandom` is now used by default. `#1052 <https://github.com/dropwizard/metrics/pull/1052>`_
* JDK's `LongAdder` is now used by default. `#1055 <https://github.com/dropwizard/metrics/pull/1055>`_
* Fixed a race condition bug in `ExponentiallyDecayingReservoir`. `#1033 <https://github.com/dropwizard/metrics/pull/1033>`_
* Fixed a long overflow bug in `SlidingTimeWindowReservoir`. `#1063 <https://github.com/dropwizard/metrics/pull/1063>`_
* `AdminServlet` supports CPU profiling. `#927 <https://github.com/dropwizard/metrics/pull/927>`_
* `GraphiteReporter` sanitizes metrics. `#938 <https://github.com/dropwizard/metrics/pull/938>`_
* Support for publishing `BigInteger` and `BigDecimal` metrics in `GraphiteReporter`. `#933 <https://github.com/dropwizard/metrics/pull/933>`_
* Support for publishing boolean metrics in `GraphiteReporter`. `#905 <https://github.com/dropwizard/metrics/pull/905>`_
* Added support for overriding the format of floating numbers in `GraphiteReporter`. `#1073 <https://github.com/dropwizard/metrics/pull/1073>`_
* Added support for disabling reporting of metric attributes. `#1048 <https://github.com/dropwizard/metrics/pull/1048>`_
* Reporters are more user friendly for managed environments like GAE or JEE. `#1018 <https://github.com/dropwizard/metrics/pull/1018>`_
* Support for setting a custom initial delay for reporters. `#999 <https://github.com/dropwizard/metrics/pull/999>`_
* Support for custom details in a result of a health check. `#663 <https://github.com/dropwizard/metrics/pull/663>`_
* Added a listener for health checks. `#1068 <https://github.com/dropwizard/metrics/pull/1068>`_
* Support for asynchronous health checks `#1077 <https://github.com/dropwizard/metrics/pull/1077>`_
* Health checks are reported as unhealthy on exceptions. `#783 <https://github.com/dropwizard/metrics/pull/783>`_
* Allow setting a custom prefix for Jetty's `InstrumentedQueuedThreadPool`. `#947 <https://github.com/dropwizard/metrics/pull/947>`_
* Allow setting custom prefix for Jetty's `QueuedThreadPool`. `#908 <https://github.com/dropwizard/metrics/pull/908>`_
* Added support for Jetty 9.3 and higher. `#1038 <https://github.com/dropwizard/metrics/pull/1038>`_
* Fixed instrumentation of Jetty9 async servlets. `#1074 <https://github.com/dropwizard/metrics/pull/1074>`_
* Added support for JCache/JSR 107 metrics. `#1010 <https://github.com/dropwizard/metrics/pull/1010>`_
* Added thread-safe getters for metrics with custom instantiations. `#1023 <https://github.com/dropwizard/metrics/pull/1023>`_
* Added an overload of `Timer#time` that takes a `Runnable`. `#989 <https://github.com/dropwizard/metrics/pull/989>`_
* Support extracting the request URI from wrapped requests in `HttpClientMetricNameStrategies`. `#947 <https://github.com/dropwizard/metrics/pull/947>`_
* Support for the log4j2 xml-based config. `#900 <https://github.com/dropwizard/metrics/pull/900>`_
* Internal `Striped64` doesn't depend on `sun.misc.Unsafe` anymore. `#966 <https://github.com/dropwizard/metrics/pull/966>`_
* Optimized creation of `UniformSnapshot`. `#970 <https://github.com/dropwizard/metrics/pull/970>`_
* Added a memory pool gauge to the JVM memory usage metrics. `#786 <https://github.com/dropwizard/metrics/pull/786>`_
* Added support for async servlets for `metric-servlet`. `#796 <https://github.com/dropwizard/metrics/pull/796>`_
* Opt-in default shared metric registry. `#801 <https://github.com/dropwizard/metrics/pull/801>`_
* Added support for patterns in MBean object names `#809 <https://github.com/dropwizard/metrics/pull/809>`_
* Allow a pluggable strategy for the name of the CSV files for `CsvReporter`. `#882 <https://github.com/dropwizard/metrics/pull/882>`_
* Upgraded to slf4j 1.22
* Upgraded to Jackson 2.6.6
* Upgraded to amqp-client 3.6.6
* Upgraded to httpclient 4.5.2
* Upgraded to log4j2 2.3
* Upgraded to logback 1.1.10

.. _rel-3.1.3:

v3.1.3: Feb 24 2017
===================

* `GraphiteReporter` opens a new TCP connection when sending metrics instead of maintaining a persisted connection. `#1036 <https://github.com/dropwizard/metrics/pull/1036>`_
* `GraphiteReporter` retries DNS lookups in case of a lookup failure. `#1064 <https://github.com/dropwizard/metrics/pull/1064>`_
* `ScheduledReporter` suppresses all kind of exceptions raised by the `report` method. `#1040 <https://github.com/dropwizard/metrics/pull/1040>`_
* JDK's `ThreadLocalRandom` is now used by default. `#1052 <https://github.com/dropwizard/metrics/pull/1052>`_
* JDK's `LongAdder` is now used by default. `#1055 <https://github.com/dropwizard/metrics/pull/1055>`_
* Fixed a race condition bug in `ExponentiallyDecayingReservoir`. `#1046 <https://github.com/dropwizard/metrics/pull/1046>`_
* Fixed a long overflow bug in `SlidingTimeWindowReservoir`. `#1072 <https://github.com/dropwizard/metrics/pull/1072>`_


.. _rel-3.1.0:

v3.1.0: Sen 10 2014
===================

https://groups.google.com/forum/#!topic/metrics-user/zwzHnMBcAX4

* Upgrade to Jetty 9.1 (metrics-jetty9, Jetty 9.0 module renamed to metrics-jetty9-legacy)
* Add log4j2 support (metrics-log4j2)
* Upgrade to Jersey2 (metrics-jersey2)
* Add httpasyncclient support (metrics-httpasyncclient)
* Changed maven groupId to io.dropwizard.metrics
* Enable Java8 builds on Travis, fix javadocs and disable some doclinting
* Fixing some compilation warnings about missing generics and varargs invocation
* Instrumentation for java.util.concurrent classes
* ExponentiallyDecayingReservoir: quantiles weighting
* Loosen type requirements for JmxAttributeGauge constructor
* SlidingWindowReservoir - ArrayOutOfBoundsException thrown if # of Reservoir examples exceeds Integer max value
* Classloader metrics
* Add an instrumented ScheduledExecutorService
* Fix race condition in InstrumentedThreadFactoryTest
* Correct comparison of System.nanoTime in SlidingTimeWindowReservoir
* Add SharedHealthCheckRegistries class
* Migrate benchmarks from Caliper to JMH
* New annotations: @CachedGauge, @Counted, @Metric
* Support for annotations on classes and constructors
* Allow @Metric on methods and parameters
* Add @Inherited and @Documented on all type annotations
* Adapted ehcache integration to latest ehcache version 2.8.3
* Upgrade to HttpClient 4.3
* InstrumentedHandler: Remove duplicate calls to requests.update(...)
* New metric 'utilization-max' to track thread usage out of max pool size in jetty
* Replaced Jetty-specific Request with Servlet API interfaces
* Jetty 8: Avoid NPE if InstrumentedQueuedThreadPool gauges are read too early
* Jetty 8: Call updateResponses onComplete of ContinuationListener
* Allow specifying a custom prefix Jetty 9 InstrumentedHandler
* MetricsModule is serializing wrong minute rates for timers
* MeterSerializer.serialize had m1_rate and m15_rate transposed
* Add CachedThreadStatesGaugeSet
* Monitor count of deadlock threads
* Prevent exceptions from ThreadDumpServlet on Google AppEngine
* Upgrade to logback 1.1.1
* Allow InstrumentedAppender use in logback.xml
* Use getClass() in place of AbstractInstrumentedFilter.class in generated metric names
* Update MetricsServlet with support for JSONP as alternative to CORS
* Specify the base name of the metrics as a filter init-param for the metrics captured in the AbstractInstrumentedFilter
* Add option to provide MetricFilter to MetricsServlet
* AdminServlet generates link to pretty printed healthchecks
* MetricsServlet.ContextListener doesn't initialize the context correctly
* Every reporter implements Reporter interface to indicate that is a Reporter
* Added support for passing a ScheduledExecutorService to ScheduledReporters
* Improve the ScheduledReporter#stop method
* Ensure ScheduledReporters get unique thread pools.
* Suppress runtime exceptions thrown from ScheduledReporter#report
* Ability to inject a factory of ObjectName
* Lazy fetch of PlatformMBeanServer
* JMX Reporter throws exception when metric name contains an asterisk
* onTimerRemoved in JmxListener calls registered.add
* Support for mBean servers that rewrite the supplied ObjectName upon registration
* Graphite reporter does not notify when Graphite/Carbon server is unreachable
* Persistent connections to Graphite
* Graphite constructor accepts host/port
* Graphtie Pickle sender
* Graphite UDP sender
* Graphite AMQP sender
* Add a threshold/minimum value to report before converting results to 0
* Report to multiple gmetric instances
* Escape slahes on ganglia metric names
* Upgrade slf4j to 1.7.6
* Enhancement for logging level option on Slf4jReporter


.. _rel-3.0.1:

v3.0.1: Jul 23 2013
===================

* Fixed NPE in ``MetricRegistry#name``.
* ``ScheduledReporter`` and ``JmxReporter`` now implement ``Closeable``.
* Fixed cast exception for async requests in ``metrics-jetty9``.
* Added support for ``Access-Control-Allow-Origin`` to ``MetricsServlet``.
* Fixed numerical issue with ``Meter`` EWMA rates.
* Deprecated ``AdminServletContextListener`` in favor of ``MetricsServlet.ContextListener`` and
  ``HealthCheckServlet.ContextListener``.
* Added additional constructors to ``HealthCheckServlet`` and ``MetricsServlet``.

.. _rel-3.0.0:

v3.0.0: June 10 2013
====================

* Renamed ``DefaultWebappMetricsFilter`` to ``InstrumentedFilter``.
* Renamed ``MetricsContextListener`` to ``InstrumentedFilterContextListener`` and made it fully
  abstract to avoid confusion.
* Renamed ``MetricsServletContextListener`` to ``AdminServletContextListener`` and made it fully
  abstract to avoid confusion.
* Upgraded to Servlet API 3.1.
* Upgraded to Jackson 2.2.2.
* Upgraded to Jetty 8.1.11.

.. _rel-3.0.0-RC1:

v3.0.0-RC1: May 31 2013
=======================

* Added ``SharedMetricRegistries``, a singleton for sharing named metric registries.
* Fixed XML configuration for ``metrics-ehcache``.
* Fixed XML configuration for ``metrics-jersey``.
* Fixed XML configuration for ``metrics-log4j``.
* Fixed XML configuration for ``metrics-logback``.
* Fixed a counting bug in ``metrics-jetty9``'s InstrumentedHandler.
* Added ``MetricsContextListener`` to ``metrics-servlet``.
* Added ``MetricsServletContextListener`` to ``metrics-servlets``.
* Extracted the ``Counting`` interface.
* Reverted ``SlidingWindowReservoir`` to a synchronized implementation.
* Added the implementation version to the JAR manifests.
* Made dependencies for all modules conform to Maven Enforcer's convergence rules.
* Fixed ``Slf4jReporter``'s logging of 99th percentiles.
* Added optional name prefixing to ``GraphiteReporter``.
* Added metric-specific overrides of rate and duration units to ``JmxReporter``.
* Documentation fixes.

.. _rel-3.0.0-BETA3:

v3.0.0-BETA3: May 13 2013
=========================

* Added ``ScheduledReporter#report()`` for manual reporting.
* Fixed overly-grabby catches in ``HealthCheck`` and
  ``InstrumentedResourceMethodDispatchProvider``.
* Fixed phantom reads in ``SlidingWindowReservoir``.
* Revamped ``metrics-jetty9``, removing ``InstrumentedConnector`` and improving
  the API.
* Fixed OSGi imports for ``sun.misc``.
* Added a strategy class for ``HttpClient`` metrics.
* Upgraded to Jetty 9.0.3.
* Upgraded to Jackson 2.2.1.
* Upgraded to Ehcache 2.6.6.
* Upgraded to Logback 1.0.13.
* Upgraded to HttpClient 4.2.5.
* Upgraded to gmetric4j 1.0.3, which allows for host spoofing.

.. _rel-3.0.0-BETA2:

v3.0.0-BETA2: Apr 22 2013
=========================

* Metrics is now under the ``com.codahale.metrics`` package, with the corresponding changes in Maven
  artifact groups. This should allow for an easier upgrade path without classpath conflicts.
* ``MetricRegistry`` no longer has a name.
* Added ``metrics-jetty9`` for Jetty 9.
* ``JmxReporter`` takes an optional domain property to disambiguate multiple reporters.
* Fixed Java 6 compatibility problem. (Also added Java 6 as a CI environment.)
* Added ``MetricRegistryListener.Base``.
* Switched ``Counter``, ``Meter``, and ``EWMA`` to use JSR133's ``LongAdder`` instead of
  ``AtomicLong``, improving contended concurrency.
* Added ``MetricRegistry#buildMap()``, allowing for custom map implementations in
  ``MetricRegistry``.
* Added ``MetricRegistry#removeMatching(MetricFilter)``.
* Changed ``metrics-json`` to optionally depend on ``metrics-healthcheck``.
* Upgraded to Jetty 8.1.10 for ``metrics-jetty8``.

.. _rel-3.0.0-BETA1:

v3.0.0-BETA1: Apr 01 2013
=========================

* Total overhaul of most of the core Metrics classes:

  * Metric names are now just dotted paths like ``com.example.Thing``, allowing for very flexible
    scopes, etc.
  * Meters and timers no longer have rate or duration units; those are properties of reporters.
  * Reporter architecture has been radically simplified, fixing many bugs.
  * Histograms and timers can take arbitrary reservoir implementations.
  * Added sliding window reservoir implementations.
  * Added ``MetricSet`` for sets of metrics.

* Changed package names to be OSGi-compatible and added OSGi bundling.
* Extracted JVM instrumentation to ``metrics-jvm``.
* Extracted Jackson integration to ``metrics-json``.
* Removed ``metrics-guice``, ``metrics-scala``, and ``metrics-spring``.
* Renamed ``metrics-servlet`` to ``metrics-servlets``.
* Renamed ``metrics-web`` to ``metrics-servlet``.
* Renamed ``metrics-jetty`` to ``metrics-jetty8``.
* Many more small changes!

.. _rel-2.2.0:

v2.2.0: Nov 26 2012
===================

* Removed all OSGi bundling. This will be back in 3.0.
* Added ``InstrumentedSslSelectChannelConnector`` and ``InstrumentedSslSocketConnector``.
* Made all metric names JMX-safe.
* Upgraded to Ehcache 2.6.2.
* Upgraded to Apache HttpClient 4.2.2.
* Upgraded to Jersey 1.15.
* Upgraded to Log4j 1.2.17.
* Upgraded to Logback 1.0.7.
* Upgraded to Spring 3.1.3.
* Upgraded to Jetty 8.1.8.
* Upgraded to SLF4J 1.7.2.
* Replaced usage of ``Unsafe`` in ``InstrumentedResourceMethodDispatchProvider`` with type erasure
  trickery.

.. _rel-2.1.5:

v2.1.5: Nov 19 2012
===================

* Upgraded to Jackson 2.1.1.

.. _rel-2.1.4:

v2.1.4: Nov 15 2012
===================

* Added OSGi bundling manifests.

.. _rel-2.1.3:

v2.1.3: Aug 06 2012
===================

* Upgraded to Apache HttpClient 4.2.1.
* Changed ``InstrumentedClientConnManager`` to extend ``PoolingClientConnectionManager`` instead of
  the deprecated ``ThreadSafeClientConnManager``.
* Fixed a bug in ``ExponentiallyDecayingSample`` with long periods of inactivity.
* Fixed problems with re-registering metrics in JMX.
* Added support for ``DnsResolver`` instances to ``InstrumentedClientConnManager``.
* Added support for formatted health check error messages.

.. _rel-2.1.2:

v2.1.2: Apr 11 2012
===================

* Fixed double-registration in ``metrics-guice``.

.. _rel-2.1.1:

v2.1.1: Mar 13 2012
===================

* Fixed instrumentation of all usages of ``InstrumentedHttpClient``.

.. _rel-2.1.0:

v2.1.0: Mar 12 2012
===================

* Added support for Java 7's direct and mapped buffer pool stats in ``VirtualMachineMetrics`` and
  ``metrics-servlet``.
* Added support for XML configuration in ``metrics-ehcache``.
* ``metrics-spring`` now support ``@Gauge``-annotated fields.
* Opened ``GraphiteReporter`` up for extension.
* Added ``group`` and ``type`` to ``metrics-annotations``, ``metrics-guice``, ``metrics-jersey``,
  and ``metrics-spring``.
* Fixed handling of non-int gauges in ``GangliaReporter``.
* Fixed ``NullPointerException`` errors in ``metrics-spring``.
* General improvements to ``metrics-spring``, including allowing custom ``Clock`` instances.

.. _rel-2.0.3:

v2.0.3: Feb 24 2012
===================

* Change logging of ``InstanceNotFoundException`` exceptions thrown while unregistering a metric
  in ``JmxReporter`` to ``TRACE``. It being ``WARN`` resulted in huge log dumps preventing process
  shutdowns when applications had ~1K+ metrics.
* Upgraded to Spring 3.1.1 for ``metrics-spring``.
* Upgraded to JDBI 2.31.2.
* Upgraded to Jersey 1.12.
* Upgraded to Jetty 7.6.1.
* Fixed rate units for meters in ``GangliaReporter``.

.. _rel-2.0.2:

v2.0.2: Feb 09 2012
===================

* ``InstrumentationModule`` in ``metrics-guice`` now uses the default ``MetricsRegistry`` and
  ``HealthCheckRegistry``.

.. _rel-2.0.1:

v2.0.1: Feb 08 2012
===================

* Fixed a concurrency bug in ``JmxReporter``.

.. _rel-2.0.0:

v2.0.0: Feb 07 2012
===================

* Upgraded to Jackson 1.9.4.
* Upgraded to Jetty 7.6.0.
* Added escaping for garbage collector and memory pool names in ``GraphiteReporter``.
* Fixed the inability to start and stop multiple reporter instances.
* Switched to using a backported version of ``ThreadLocalRandom`` for ``UniformSample`` and
  ``ExponentiallyDecayingSample`` to reduce lock contention on random number generation.
* Removed ``Ordered`` from ``TimedAnnotationBeanPostProcessor`` in ``metrics-spring``.
* Upgraded to JDBI 2.31.1.
* Upgraded to Ehcache 2.5.1.
* Added ``#timerContext()`` to Scala ``Timer``.

.. _rel-2.0.0-RC0:

v2.0.0-RC0: Jan 19 2012
=======================

* Added FindBugs checks to the build process.
* Fixed the catching of ``Error`` instances thrown during health checks.
* Added ``enable`` static methods to ``CsvReporter`` and changed
  ``CsvReporter(File, MetricsRegistry)`` to ``CsvReporter(MetricsRegistry, File)``.
* Slimmed down ``InstrumentedEhcache``.
* Hid the internals of ``GangliaReporter``.
* Hid the internals of ``metrics-guice``.
* Changed ``metrics-httpclient`` to consistently associate metrics with the ``org.apache`` class
  being extended.
* Hid the internals of ``metrics-httpclient``.
* Rewrote ``InstrumentedAppender`` in ``metrics-log4j``. It no longer forwards events to an
  appender. Instead, you can just attach it to your root logger to instrument logging.
* Rewrote ``InstrumentedAppender`` in ``metrics-logback``. No major API changes.
* Fixed bugs with ``@ExceptionMetered``-annotated resource methods in ``metrics-jersey``.
* Fixed bugs generating ``Snapshot`` instances from concurrently modified collections.
* Fixed edge case in ``MetricsServlet``'s thread dumps where one thread could be missed.
* Added ``RatioGauge`` and ``PercentGauge``.
* Changed ``InstrumentedQueuedThreadPool``'s ``percent-idle`` gauge to be a ratio.
* Decomposed ``MetricsServlet`` into a set of focused servlets: ``HealthCheckServlet``,
  ``MetricsServlet``, ``PingServlet``, and ``ThreadDumpServlet``. The top-level servlet which
  provides the HTML menu page is now ``AdminServlet``.
* Added ``metrics-spring``.

.. _rel-2.0.0-BETA19:

v2.0.0-BETA19: Jan 07 2012
==========================

* Added absolute memory usage to ``MetricsServlet``.
* Extracted ``@Timed`` etc. to ``metrics-annotations``.
* Added ``metrics-jersey``, which provides a class allowing you to automatically instrument all
  ``@Timed``, ``@Metered``, and ``@ExceptionMetered``-annotated resource methods.
* Moved all classes in ``metrics-scala`` from ``com.yammer.metrics`` to
  ``com.yammer.metrics.scala``.
* Renamed ``CounterMetric`` to ``Counter``.
* Renamed ``GaugeMetric`` to ``Gauge``.
* Renamed ``HistogramMetric`` to ``Histogram``.
* Renamed ``MeterMetric`` to ``Meter``.
* Renamed ``TimerMetric`` to ``Timer``.
* Added ``ToggleGauge``, which returns ``1`` the first time it's called and ``0`` every time after
  that.
* Now licensed under Apache License 2.0.
* Converted ``VirtualMachineMetrics`` to a non-singleton class.
* Removed ``Utils``.
* Removed deprecated constructors from ``Meter`` and ``Timer``.
* Removed ``LoggerMemoryLeakFix``.
* ``DeathRattleExceptionHandler`` now logs to SLF4J, not syserr.
* Added ``MetricsRegistry#groupedMetrics()``.
* Removed ``Metrics#allMetrics()``.
* Removed ``Metrics#remove(MetricName)``.
* Removed ``MetricsRegistry#threadPools()`` and ``#newMeterTickThreadPool()`` and added
  ``#newScheduledThreadPool``.
* Added ``MetricsRegistry#shutdown()``.
* Renamed ``ThreadPools#shutdownThreadPools()`` to ``#shutdown()``.
* Replaced ``HealthCheck``'s abstract ``name`` method with a required constructor parameter.
* ``HealthCheck#check()`` is now ``protected``.
* Moved ``DeadlockHealthCheck`` from ``com.yammer.metrics.core`` to ``com.yammer.metrics.utils``.
* Added ``HealthCheckRegistry#unregister(HealthCheck)``.
* Fixed typo in ``VirtualMachineMetrics`` and ``MetricsServlet``: ``commited`` to ``committed``.
* Changed ``MetricsRegistry#createName`` to ``protected``.
* All metric types are created exclusively through ``MetricsRegistry`` now.
* ``Metrics.newJmxGauge`` and ``MetricsRegistry.newJmxGauge`` are deprecated.
* Fixed heap metrics in ``VirtualMachineMetrics``.
* Added ``Snapshot``, which calculates quantiles.
* Renamed ``Percentiled`` to ``Sampling`` and dropped ``percentile`` and ``percentiles`` in favor of
  producing ``Snapshot`` instances. This affects both ``Histogram`` and ``Timer``.
* Renamed ``Summarized`` to ``Summarizable``.
* Changed order of ``CsvReporter``'s construction parameters.
* Renamed ``VirtualMachineMetrics.GarbageCollector`` to
  ``VirtualMachineMetrics.GarbageCollectorStats``.
* Moved Guice/Servlet support from ``metrics-servlet`` to ``metrics-guice``.
* Removed ``metrics-aop``.
* Removed ``newJmxGauge`` from both ``Metrics`` and ``MetricsRegistry``. Just use ``JmxGauge``.
* Moved ``JmxGauge`` to ``com.yammer.metrics.util``.
* Moved ``MetricPredicate`` to ``com.yammer.metrics.core``.
* Moved ``NameThreadFactory`` into ``ThreadPools`` and made ``ThreadPools`` package-visible.
* Removed ``Timer#values()``, ``Histogram#values()``, and ``Sample#values()``. Use ``getSnapshot()``
  instead.
* Removed ``Timer#dump(File)`` and ``Histogram#dump(File)``, and ``Sample#dump(File)``. Use
  ``Snapshot#dump(File)`` instead.

.. _rel-2.0.0-BETA18:

v2.0.0-BETA18: Dec 16 2011
==========================

* Added ``DeathRattleExceptionHandler``.
* Fixed NPE in ``VirtualMachineMetrics``.
* Added decorators for connectors and thread pools in ``metrics-jetty``.
* Added ``TimerMetric#time()`` and ``TimerContext``.
* Added a shorter factory method for millisecond/second timers.
* Switched tests to JUnit.
* Improved logging in ``GangliaReporter``.
* Improved random number generation for ``UniformSample``.
* Added ``metrics-httpclient`` for instrumenting Apache HttpClient 4.1.
* Massively overhauled the reporting code.
* Added support for instrumented, non-``public`` methods in ``metrics-guice``.
* Added ``@ExceptionMetered`` to ``metrics-guice``.
* Added group prefixes to ``GangliaReporter``.
* Added ``CvsReporter``, which outputs metric values to ``.csv`` files.
* Improved metric name sanitization in ``GangliaReporter``.
* Added ``Metrics.shutdown()`` and improved metrics lifecycle behavior.
* Added ``metrics-web``.
* Upgraded to ehcache 2.5.0.
* Many, many refactorings.
* ``metrics-servlet`` now responds with ``501 Not Implememented`` when no health checks have been
  registered.
* Many internal refactorings for testability.
* Added histogram counts to ``metrics-servlet``.
* Fixed a race condition in ``ExponentiallyDecayingSample``.
* Added timezone and locale support to ``ConsoleReporter``.
* Added ``metrics-aop`` for Guiceless support of method annotations.
* Added ``metrics-jdbi`` which adds instrumentation to JDBI_.
* Fixed NPE for metrics which belong to classes in the default package.
* Now deploying artifacts to Maven Central.

.. _JDBI: http://www.jdbi.org

.. _rel-2.0.0-BETA17:

v2.0.0-BETA17: Oct 07 2011
==========================

* Added an option message to successful health check results.
* Fixed locale issues in ``GraphiteReporter``.
* Added ``GangliaReporter``.
* Added per-HTTP method timers to ``InstrumentedHandler`` in ``metrics-jetty``.
* Fixed a thread pool leak for meters.
* Added ``#dump(File)`` to ``HistogramMetric`` and ``TimerMetric``.
* Upgraded to Jackson 1.9.x.
* Upgraded to slf4j 1.6.2.
* Upgraded to logback 0.9.30.
* Upgraded to ehcache 2.4.5.
* Surfaced ``Metrics.removeMetric()``.

.. _rel-2.0.0-BETA16:

v2.0.0-BETA16: Aug 23 2011
==========================

* Fixed a bug in GC monitoring.

.. _rel-2.0.0-BETA15:

v2.0.0-BETA15: Aug 15 2011
==========================

* Fixed dependency scopes for ``metrics-jetty``.
* Added time and VM version to ``vm`` output of ``MetricsServlet``.
* Dropped ``com.sun.mangement``-based GC instrumentation in favor of a
  ``java.lang.management``-based one. ``getLastGcInfo`` has a nasty native memory leak in it, plus
  it often returned incorrect data.
* Upgraded to Jackson 1.8.5.
* Upgraded to Jetty 7.4.5.
* Added sanitization for metric names in ``GraphiteReporter``.
* Extracted out a ``Clock`` interface for timers for non-wall-clock timing.
* Extracted out most of the remaining statics into ``MetricsRegistry`` and ``HealthCheckRegistry``.
* Added an init parameter to ``MetricsServlet`` for disabling the ``jvm`` section.
* Added a Guice module for ``MetricsServlet``.
* Added dynamic metric names.
* Upgraded to ehcache 2.4.5.
* Upgraded to logback 0.9.29.
* Allowed for the removal of metrics.
* Added the ability to filter metrics exposed by a reporter to those which match a given predicate.

.. _rel-2.0.0-BETA14:

v2.0.0-BETA14: Jul 05 2011
==========================

* Moved to Maven for a build system and extracted the Scala façade to a ``metrics-scala`` module
  which is now the only cross-built module. All other modules dropped the Scala version suffix in
  their ``artifactId``.
* Fixed non-heap metric name in ``GraphiteReporter``.
* Fixed stability error in ``GraphiteReporter`` when dealing with unavailable servers.
* Fixed error with anonymous, instrumented classes.
* Fixed error in ``MetricsServlet`` when a gauge throws an exception.
* Fixed error with bogus GC run times.
* Link to the pretty JSON output from the ``MetricsServlet`` menu page.
* Fixed potential race condition in histograms' variance calculations.
* Fixed memory pool reporting for the G1 collector.

.. _rel-2.0.0-BETA13:

v2.0.0-BETA13: May 13 2011
==========================

* Fixed a bug in the initial startup phase of the ``JmxReporter``.
* Added ``metrics-ehcache``, for the instrumentation of ``Ehcache`` instances.
* Fixed a typo in ``metrics-jetty``'s ``InstrumentedHandler``.
* Added name prefixes to ``GraphiteReporter``.
* Added JVM metrics reporting to ``GraphiteReporter``.
* Actually fixed ``MetricsServlet``'s links when the servlet has a non-root context path.
* Now cross-building for Scala 2.9.0.
* Added ``pretty`` query parameter for ``MetricsServlet`` to format the JSON object for human
  consumption.
* Added ``no-cache`` headers to the ``MetricsServlet`` responses.

.. _rel-2.0.0-BETA12:

v2.0.0-BETA12: May 09 2011
==========================

* Upgraded to Jackson 1.7.6.
* Added a new instrumented Log4J appender.
* Added a new instrumented Logback appender. Thanks to Bruce Mitchener
  (@waywardmonkeys) for the patch.
* Added a new reporter for the Graphite_ aggregation system. Thanks to Mahesh Tiyyagura (@tmahesh)
  for the patch.
* Added scoped metric names.
* Added Scala 2.9.0.RC{2,3,4} as build targets.
* Added meters to Jetty handler for the percent of responses which have ``4xx`` or ``5xx`` status
  codes.
* Changed the Servlet API to be a ``provided`` dependency. Thanks to Mårten Gustafson (@chids) for
  the patch.
* Separated project into modules:

  * ``metrics-core``: A dependency-less project with all the core metrics.
  * ``metrics-graphite``: A reporter for the [Graphite](http://graphite.wikidot.com)
    aggregation system.
  * ``metrics-guice``: Guice AOP support.
  * ``metrics-jetty``: An instrumented Jetty handler.
  * ``metrics-log4j``: An instrumented Log4J appender.
  * ``metrics-logback``: An instrumented Logback appender.
  * ``metrics-servlet``: The Metrics servlet with context listener.

.. _Graphite: http://graphite.wikidot.com

.. _rel-2.0.0-BETA11:

v2.0.0-BETA11: Apr 27 2011
==========================

* Added thread state and deadlock detection metrics.
* Fix ``VirtualMachineMetrics``' initialization.
* Context path fixes for the servlet.
* Added the ``@Gauge`` annotation.
* Big reworking of the exponentially-weighted moving average code for meters. Thanks to JD Maturen
  (@sku) and John Ewart (@johnewart) for pointing this out.
* Upgraded to Guice 3.0.
* Upgraded to Jackson 1.7.5.
* Upgraded to Jetty 7.4.0.
* Big rewrite of the servlet's thread dump code.
* Fixed race condition in ``ExponentiallyDecayingSample``. Thanks to Martin Traverso (@martint) for
  the patch.
* Lots of spelling fixes in Javadocs. Thanks to Bruce Mitchener (@waywardmonkeys) for the patch.
* Added Scala 2.9.0.RC1 as a build target. Thanks to Bruce Mitchener (@waywardmonkeys) for the
  patch.
* Patched a hilarious memory leak in ``java.util.logging``.

.. _rel-2.0.0-BETA10:

v2.0.0-BETA10: Mar 25 2011
==========================

* Added Guice AOP annotations: ``@Timed`` and ``@Metered``.
* Added ``HealthCheck#name()``.
* Added ``Metrics.newJmxGauge()``.
* Moved health checks into ``HealthChecks``.
* Upgraded to Jackson 1.7.3 and Jetty 7.3.1.

.. _rel-2.0.0-BETA9:

v2.0.0-BETA9: Mar 14 2011
=========================

* Fixed ``JmxReporter`` lag.
* Added default arguments to timers and meters.
* Added default landing page to the servlet.
* Improved the performance of ``ExponentiallyDecayingSample``.
* Fixed an integer overflow bug in ``UniformSample``.
* Added linear scaling to ``ExponentiallyDecayingSample``.

.. _rel-2.0.0-BETA8:

v2.0.0-BETA8: Mar 01 2011
=========================

* Added histograms.
* Added biased sampling for timers.
* Added dumping of timer/histogram samples via the servlet.
* Added dependency on ``jackon-mapper``.
* Added classname filtering for the servlet.
* Added URI configuration for the servlet.

.. _rel-2.0.0-BETA7:

v2.0.0-BETA7: Jan 12 2011
=========================

* Added ``JettyHandler``.
* Made the ``Servlet`` dependency optional.

.. _rel-2.0.0-BETA6:

v2.0.0-BETA6: Jan 12 2011
=========================

* Fix ``JmxReporter`` initialization.

.. _rel-2.0.0-BETA5:

v2.0.0-BETA5: Jan 11 2011
=========================

* Dropped ``Counter#++`` and ``Counter#--``.
* Added ``Timer#update``.
* Upgraded to Jackson 1.7.0.
* Made JMX reporting implicit.
* Added health checks.

.. _rel-2.0.0-BETA3:

v2.0.0-BETA3: Dec 23 2010
=========================

* Fixed thread names and some docs.

.. _rel-2.0.0-BETA2:

v2.0.0-BETA2: Dec 22 2010
=========================

* Fixed a memory leak in ``MeterMetric``.

.. _rel-2.0.0-BETA1:

v2.0.0-BETA1: Dec 22 2010
=========================

* Total rewrite in Java.

.. _rel-1.0.7:

v1.0.7: Sep 21 2010
===================

* Added ``median`` to ``Timer``.
* Added ``p95`` to ``Timer`` (95th percentile).
* Added ``p98`` to ``Timer`` (98th percentile).
* Added ``p99`` to ``Timer`` (99th percentile).

.. _rel-1.0.6:

v1.0.6: Jul 15 2010
===================

* Now compiled exclusively for 2.8.0 final.

.. _rel-1.0.5:

v1.0.5: Jun 01 2010
===================

* Documentation fix.
* Added ``TimedToggle``, which may or may not be useful at all.
* Now cross-building for RC2 and RC3.

.. _rel-1.0.4:

v1.0.4: Apr 27 2010
===================

* Blank ``Timer`` instances (i.e., those which have recorded no timings yet) no longer explode when
  asked for metrics for that which does not yet exist.
* Nested classes, companion objects, and singletons don't have trailing ``$`` characters messing up
  JMX's good looks.

.. _rel-1.0.3:

v1.0.3: Apr 16 2010
===================

* Fixed some issues with the `implicit.ly`__ plumbing.
* Tweaked the sample size for ``Timer``, giving it 99.9% confidence level with a %5 margin of error
  (for a normally distributed variable, which it almost certainly isn't.)
* ``Sample#iterator`` returns only the recorded data, not a bunch of zeros.
* Moved units of ``Timer``, ``Meter``, and ``LoadMeter`` to their own attributes, which allows for
  easy export of Metrics data via JMX to things like Ganglia__ or whatever.
  
.. __: http://implicit.ly
.. __: http://ganglia.sourceforge.net/

.. _rel-1.0.2:

v1.0.2: Mar 08 2010
===================

* ``Timer`` now uses Welford's algorithm for calculating running variance, which means no more
  hilariously wrong standard deviations (e.g., ``NaN``).
* ``Timer`` now supports ``+=(Long)`` for pre-recorded, nanosecond-precision timings.

.. _rel-1.0.1:

v1.0.1: Mar 05 2010
===================

* changed ``Sample`` to use an ``AtomicReferenceArray``

.. _rel-1.0.0:

v1.0.0: Feb 27 2010
===================

* Initial release
