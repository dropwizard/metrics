.. _release-notes:

#############
Release Notes
#############

.. _rel-3.0.0:

v3.0.0-SNAPSHOT
===============

* Added ``AdminServlet#setServiceName()``.
* Switched all getters to the standard ``#getValue()``.
* Use the full metric name in ``CsvReporter``.
* Made ``DefaultWebappMetricsFilter``'s registry configurable.
* Switched to ``HttpServletRequest#getContextPath()`` in ``AdminServlet``.
* Upgraded to Logback 1.0.3.
* Upgraded to Log4j 1.2.17.
* Upgraded to JDBI 2.34.
* Upgraded to Ehcache 2.5.2.
* Upgraded to Jackson 2.0.2.
* Upgraded to Jetty 8.1.4.
* Upgraded to SLF4J 1.6.5.
* Changed package names in ``metrics-ganglia``, ``metrics-graphite``, and ``metrics-servlet``.
* Removed ``metrics-guice`` and ``metrics-spring``.

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
