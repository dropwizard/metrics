.. _manual-guice:

#################
Guice Integration
#################

The ``metrics-guice`` module provides integration with Google's Guice_ dependency injection
framework.

.. _Guice: http://code.google.com/p/google-guice/

The ``InstrumentationModule`` class adds a set of method interceptors to your ``Injector`` which
leverage Guice's AOP support to add meters to ``@Metered``-annotated methods, timers to
``@Timed``-annotated methods, exception meters to ``@ExceptionMetered``-annotated methods, and
gauges for the results of ``@Gauge``-annotated methods.

The ``JmxReporterProvider`` class is a ``Provider`` implementation which gives you access to a
started ``JmxReporter`` instance.

Finally, the ``AdminServletModule`` adds support for ``metric-servlet``'s ``AdminServlet`` to your
Guice Servlet configuration.
