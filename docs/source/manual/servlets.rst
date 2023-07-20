.. _manual-servlets:

################
Metrics Servlets
################

The ``metrics-servlets`` module provides a handful of useful servlets:

.. _man-servlet-healthcheck:

HealthCheckServlet
==================

``HealthCheckServlet`` responds to ``GET`` requests by running all the currently-registered
[health checks](#health-checks). The results are returned as a human-readable JSON entity.

HTTP Status Codes
-----------------

``HealthCheckServlet`` responds with one of the following status codes (depending on configuration).
If reporting health via HTTP status is disabled, callers will have to introspect the JSON to
determine application health.

* ``501 Not Implemented``: If no health checks are registered
* ``200 OK``: If all checks pass, or if ``httpStatusIndicator`` is set to ``"false"`` and one or more
  health checks fail (see below for more information on this setting)
* ``500 Internal Service Error``: If ``httpStatusIndicator`` is set to ``"true"`` and one or more
  health checks fail (see below for more information on this setting)

Configuration
-------------

``HealthCheckServlet`` supports the following configuration items.

Servlet Context
~~~~~~~~~~~~~~~

``HealthCheckServlet`` requires that the servlet context has a ``HealthCheckRegistry`` named
``io.dropwizard.metrics5.servlets.HealthCheckServlet.registry``. You can subclass
``HealthCheckServlet.ContextListener``, which will add a specific ``HealthCheckRegistry`` to the
servlet context.

An instance of ``ExecutorService`` can be provided via the servlet context using the name
``io.dropwizard.metrics5.servlets.HealthCheckServlet.executor``; by default, no thread pool is used to
execute the health checks.

An instance of ``HealthCheckFilter`` can be provided via the servlet context using the name
``io.dropwizard.metrics5.servlets.HealthCheckServlet.healthCheckFilter``; by default, no filtering is
enabled. The filter is used to determine which health checks to include in the health status.

An instance of ``ObjectMapper`` can be provided via the servlet context using the name
``io.dropwizard.metrics5.servlets.HealthCheckServlet.mapper``; if none is provided, a default instance
will be used to convert the health check results to JSON.

Initialization Parameters
~~~~~~~~~~~~~~~~~~~~~~~~~

``HealthCheckServlet`` supports the following initialization parameters:

* ``io.dropwizard.metrics5.servlets.HealthCheckServlet.httpStatusIndicator``: Provides the
  default setting that determines whether the HTTP status code is used to determine whether the
  application is healthy; if not provided, it defaults to ``"true"``

Query Parameters
~~~~~~~~~~~~~~~~

``HealthCheckServlet`` supports the following query parameters:

* ``httpStatusIndicator`` (``Boolean``): Determines whether the HTTP status code is used to
  determine whether the application is healthy; if not provided, it defaults to the value from the
  initialization parameter
* ``pretty`` (``Boolean``): Indicates whether the JSON response should be formatted; if
  ``"true"``, the JSON response will be formatted instead of condensed

.. _man-servlet-threaddump:

ThreadDumpServlet
=================

``ThreadDumpServlet`` responds to ``GET`` requests with a ``text/plain`` representation of all the live
threads in the JVM, their states, their stack traces, and the state of any locks they may be
waiting for.

Configuration
-------------

``ThreadDumpServlet`` supports the following configuration items.

Query Parameters
~~~~~~~~~~~~~~~~

``ThreadDumpServlet`` supports the following query parameters:

* ``monitors`` (``Boolean``): Determines whether locked monitors are included; if not provided,
  it defaults to ``"true"``
* ``synchronizers`` (``Boolean``): Determines whether locked ownable synchronizers are included;
  if not provided, it defaults to ``"true"``

.. _man-servlet-metrics:

MetricsServlet
==============

``MetricsServlet`` exposes the state of the metrics in a particular registry as a JSON object.

Configuration
-------------

``MetricsServlet`` supports the following configuration items.

Servlet Context
~~~~~~~~~~~~~~~

``MetricsServlet`` requires that the servlet context has a ``MetricRegistry`` named
``io.dropwizard.metrics5.servlets.MetricsServlet.registry``. You can subclass
``MetricsServlet.ContextListener``, which will add a specific ``MetricRegistry`` to the servlet
context.

An instance of ``MetricFilter`` can be provided via the servlet context using the name
``io.dropwizard.metrics5.servlets.MetricsServlet.metricFilter``; by default, no filtering is
enabled. The filter is used to determine which metrics to include in the JSON output.

Initialization Parameters
~~~~~~~~~~~~~~~~~~~~~~~~~

``MetricsServlet`` supports the following initialization parameters:

* ``io.dropwizard.metrics5.servlets.MetricsServlet.allowedOrigin``: Provides a value for the
  response header ``Access-Control-Allow-Origin``; if no value is provided, the header is not used
* ``io.dropwizard.metrics5.servlets.MetricsServlet.jsonpCalblack``: Specifies a request parameter
  name to use as the callback when returning the metrics as JSON-P; if no value is provided, the response is
  returned as JSON. This also requires a query parameter with the same name as the value to enable a JSON-P
  response.
* ``io.dropwizard.metrics5.servlets.MetricsServlet.rateUnit``: Provides a value for the
  rate unit used for metrics output; if none is provided, the default is ``SECONDS`` (see ``TimeUnit`` for
  acceptable values)
* ``io.dropwizard.metrics5.servlets.MetricsServlet.durationUnit``: Provides a value for the
  duration unit used for metrics output; if none is provided, the default is ``SECONDS`` (see ``TimeUnit`` for
  acceptable values)
* ``io.dropwizard.metrics5.servlets.MetricsServlet.showSamples``: Controls whether sample data is
  included in the output for histograms and timers; if no value is provided, the sample data will be omitted.

Query Parameters
~~~~~~~~~~~~~~~~

``MetricsServlet`` supports the following query parameters:

* ``pretty`` (``Boolean``): Determines whether the results are formatted; if not provided, this
  parameter defaults to ``"false"``.

.. _man-servlet-ping:

PingServlet
===========

``PingServlet`` responds to ``GET`` requests with a ``text/plain``/``200 OK`` response of ``pong``. This is
useful for determining liveness for load balancers, etc.

.. _man-servlet-cpu-profile:

CpuProfileServlet
=================

``CpuProfileServlet`` responds to ``GET`` requests with a ``pprof/raw``/``200 OK`` response containing the
results of CPU profiling.

Configuration
-------------

``CpuProfileServlet`` supports the following configuration items.

Query Parameters
~~~~~~~~~~~~~~~~

``CpuProfileServlet`` supports the following query parameters:

* ``duration`` (``Integer``): Determines the amount of time in seconds for which the CPU
  profiling will occur; the default is 10 seconds.
* ``frequency`` (``Integer``)Determines the frequency in Hz at which the CPU
  profiling sample; the default is 100 Hz (100 times per second).
* ``state`` (``String``): Determines which threads will be profiled. If the value provided
  is ``"blocked"``, only blocked threads will be profiled; otherwise, all runnable threads will be
  profiled.

.. _man-servlet-admin:

AdminServlet
============

``AdminServlet`` aggregates ``HealthCheckServlet``, ``ThreadDumpServlet``, ``MetricsServlet``, and
``PingServlet`` into a single, easy-to-use servlet which provides a set of URIs:

* ``/``: an HTML admin menu with links to the following:

  * ``/metrics``: ``MetricsServlet``
    * To change the URI, set the
  * ``/ping``: ``PingServlet``
  * ``/threads``: ``ThreadDumpServlet``
  * ``/healthcheck``: ``HealthCheckServlet``
  * ``/pprof``: ``CpuProfileServlet``
    * There will be two links; one for the base profile and one for CPU contention

You will need to add your ``MetricRegistry`` and ``HealthCheckRegistry`` instances to the servlet
context as attributes named ``io.dropwizard.metrics5.servlets.MetricsServlet.registry`` and
``io.dropwizard.metrics5.servlets.HealthCheckServlet.registry``, respectively. You can do this using
the Servlet API by extending ``MetricsServlet.ContextListener`` for MetricRegistry:

.. code-block:: java

    public class MyMetricsServletContextListener extends MetricsServlet.ContextListener {

        public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

        @Override
        protected MetricRegistry getMetricRegistry() {
            return METRIC_REGISTRY;
        }

    }

And by extending ``HealthCheckServlet.ContextListener`` for HealthCheckRegistry:

.. code-block:: java

    public class MyHealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

        public static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

        @Override
        protected HealthCheckRegistry getHealthCheckRegistry() {
            return HEALTH_CHECK_REGISTRY;
        }

    }

Then you will need to register servlet context listeners either in you ``web.xml`` or annotating the class
with ``@WebListener`` if you are in servlet 3.0 environment. In ``web.xml``:

.. code-block:: xml

	<listener>
		<listener-class>com.example.MyMetricsServletContextListener</listener-class>
	</listener>
	<listener>
		<listener-class>com.example.MyHealthCheckServletContextListener</listener-class>
	</listener>

You will also need to register ``AdminServlet`` in ``web.xml``:

.. code-block:: xml

 	<servlet>
		<servlet-name>metrics</servlet-name>
		<servlet-class>io.dropwizard.metrics5.servlets.AdminServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>metrics</servlet-name>
		<url-pattern>/metrics/*</url-pattern>
	</servlet-mapping>

Configuration
-------------

``AdminServlet`` supports the following configuration items.

Initialization Parameters
~~~~~~~~~~~~~~~~~~~~~~~~~

``AdminServlet`` supports the following initialization parameters:

* ``metrics-enabled``: Determines whether the ``MetricsServlet`` is enabled and
  routable; if ``"false"``, the servlet endpoint will not be available via this servlet
* ``metrics-uri``: Specifies the URI for the ``MetricsServlet``; if omitted, the default
  (``/metrics``) will be used
* ``ping-enabled``: Determines whether the ``PingServlet`` is enabled and routable; if
  ``"false"``, the servlet endpoint will not be available via this servlet
* ``ping-uri``: Specifies the URI for the ``PingServlet``; if omitted, the default
  (``/ping``) will be used
* ``threads-enabled``: Determines whether the ``ThreadDumpServlet`` is enabled
  and routable; if ``"false"``, the servlet endpoint will not be available via this servlet
* ``threads-uri``: Specifies the URI for the ``ThreadDumpServlet``; if omitted, the default
  (``/threads``) will be used
* ``cpu-profile-enabled``: Determines whether the ``CpuProfileServlet`` is enabled and routable;
  if ``"false"``, the servlet endpoints will not be available via this servlet
* ``cpu-profile-uri``: Specifies the URIs for the ``CpuProfileServlet``; if omitted, the default
  (``/pprof``) will be used
