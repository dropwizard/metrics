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
``text/plain`` entity.

``HealthCheckServlet`` requires that the servlet context has a ``HealthCheckRegistry`` named
``io.dropwizard.metrics5.servlets.HealthCheckServlet.registry``. You can subclass
``MetricsServletContextListener``, which will add a specific ``HealthCheckRegistry`` to the servlet
context.

.. _man-servlet-threaddump:

ThreadDumpServlet
=================

``ThreadDumpServlet`` responds to ``GET`` requests with a ``text/plain`` representation of all the live
threads in the JVM, their states, their stack traces, and the state of any locks they may be
waiting for.

.. _man-servlet-metrics:

MetricsServlet
==============

``MetricsServlet`` exposes the state of the metrics in a particular registry as a JSON object.

``MetricsServlet`` requires that the servlet context has a ``MetricRegistry`` named
``io.dropwizard.metrics5.servlets.MetricsServlet.registry``. You can subclass
``MetricsServletContextListener``, which will add a specific ``MetricRegistry`` to the servlet
context.

``MetricsServlet`` also takes an initialization parameter, ``show-jvm-metrics``, which if ``"false"`` will
disable the outputting of JVM-level information in the JSON object.

.. _man-servlet-ping:

PingServlet
===========

``PingServlet`` responds to ``GET`` requests with a ``text/plain``/``200 OK`` response of ``pong``. This is
useful for determining liveness for load balancers, etc.

.. _man-servlet-admin:

AdminServlet
============

``AdminServlet`` aggregates ``HealthCheckServlet``, ``ThreadDumpServlet``, ``MetricsServlet``, and
``PingServlet`` into a single, easy-to-use servlet which provides a set of URIs:

* ``/``: an HTML admin menu with links to the following:

  * ``/healthcheck``: ``HealthCheckServlet``
  * ``/metrics``: ``MetricsServlet``
  * ``/ping``: ``PingServlet``
  * ``/threads``: ``ThreadDumpServlet``

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
		<servlet-class>io.dropwizard.metrics5.servlets.AdminServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>metrics</servlet-name>
		<url-pattern>/metrics/*</url-pattern>
	</servlet-mapping>


