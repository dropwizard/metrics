.. _manual-servlets:

################
Metrics Servlets
################

The ``metrics-servlets`` module provides a handful of useful servlets:

.. _man-servlet-healthcheck:

HealthCheckServlet
==================

``HealthCheckServlet`` responds to ``GET`` requests by running all the [health checks](#health-checks)
and returning ``501 Not Implemented`` if no health checks are registered, ``200 OK`` if all pass, or
``500 Internal Service Error`` if one or more fail. The results are returned as a human-readable
``application/json`` entity.

Configuration
-------------

Servlet Context
~~~~~~~~~~~~~~~

``HealthCheckServlet`` requires that the servlet context has a ``HealthCheckRegistry`` named
``com.codahale.metrics.servlets.HealthCheckServlet.registry``. You can subclass
``MetricsServletContextListener``, which will add a specific ``HealthCheckRegistry`` to the servlet
context.

Initialization Parameters
~~~~~~~~~~~~~~~~~~~~~~~~~

``HealthCheckServlet`` takes an initialization parameter named
``com.codahale.metrics.servlets.HealthCheckServlet.httpStatusIndicator``. This parameter provides the
default setting that determines whether the HTTP status code is used to determine whether the
application is healthy. If not provided, it defaults to ``"true"``.

Query Parameters
~~~~~~~~~~~~~~~~~~~~~~~~~

``HealthCheckServlet`` takes the following query parameters:

* ``httpStatusIndicator``: This Boolean parameter determines whether the HTTP status code is used to
  determine whether the application is healthy; if not provided, it defaults to the value from the
  initialization parameter
* ``pretty``: This Boolean parameter indicates whether the JSON response should be formatted; if
  ``"true"``, the JSON response will be formatted instead of condensed

.. _man-servlet-threaddump:

ThreadDumpServlet
=================

``ThreadDumpServlet`` responds to ``GET`` requests with a ``text/plain`` representation of all the live
threads in the JVM, their states, their stack traces, and the state of any locks they may be
waiting for.

Configuration
-------------

``ThreadDumpServlet`` takes the following query parameters:

* ``monitors``: This Boolean parameter determines whether locked monitors are included; if not provided,
  it defaults to ``"true"``
* ``synchronizers``: This Boolean parameter determines whether locked ownable synchronizers are included;
  if not provided, it defaults to ``"true"``

.. _man-servlet-metrics:

MetricsServlet
==============

``MetricsServlet`` exposes the state of the metrics in a particular registry as a JSON object.

``MetricsServlet`` requires that the servlet context has a ``MetricRegistry`` named
``com.codahale.metrics.servlets.MetricsServlet.registry``. You can subclass
``MetricsServletContextListener``, which will add a specific ``MetricRegistry`` to the servlet
context.

``MetricsServlet`` takes an initialization parameter, ``show-jvm-metrics``, which if ``"false"`` will
disable the outputting of JVM-level information in the JSON object.

.. _man-servlet-ping:

PingServlet
===========

``PingServlet`` responds to ``GET`` requests with a ``text/plain``/``200 OK`` response of ``pong``. This is
useful for determining liveness for load balancers, etc.

.. _man-servlet-cpu-profile:

CpuProfileServlet
===========

``CpuProfileServlet`` responds to ``GET`` requests with a ``pprof/raw``/``200 OK`` response containing the results of a
CPU profile.

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
context as attributes named ``com.codahale.metrics.servlets.MetricsServlet.registry`` and
``com.codahale.metrics.servlets.HealthCheckServlet.registry``, respectively. You can do this using
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

Then you will need to register servlet context listeners either in you ``web.xml`` or annotating the class with ``@WebListener`` if you are in servlet 3.0 environment. In ``web.xml``:

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
		<servlet-class>com.codahale.metrics.servlets.AdminServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>metrics</servlet-name>
		<url-pattern>/metrics/*</url-pattern>
	</servlet-mapping>


