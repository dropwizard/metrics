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
``com.codahale.metrics.servlets.HealthCheckServlet.registry``. You can subclass
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
``com.codahale.metrics.servlets.MetricsServlet.registry``. You can subclass
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

MetricsServletContextListener
=============================

``MetricsServletContextListener`` is a ServletContextListener that can be used to set up registries
that are required by the ``AdminServlet``. It receieves ``MetricRegistry`` and
``HealthCheckRegistry`` instances as constructor parameters, and adds them to the ServletContext as attributes.


