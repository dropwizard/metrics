.. _manual-jetty:

###################
Instrumenting Jetty
###################

The ``metrics-jetty9``(Jetty 9.3 and higher) modules provides a set of instrumented equivalents of Jetty_ classes:
``InstrumentedBlockingChannelConnector``, ``InstrumentedHandler``, ``InstrumentedQueuedThreadPool``,
``InstrumentedSelectChannelConnector``, and ``InstrumentedSocketConnector``.

.. _Jetty: https://www.eclipse.org/jetty/

The ``Connector`` implementations are simple, instrumented subclasses of the Jetty connector types
which measure connection duration, the rate of accepted connections, connections, disconnections,
and the total number of active connections.

``InstrumentedQueuedThreadPool`` is a ``QueuedThreadPool`` subclass which measures the ratio of idle
threads to working threads as well as the absolute number of threads (idle and otherwise).

``InstrumentedHandler`` is a ``Handler`` decorator which measures a wide range of HTTP behavior:
dispatch times, requests, resumes, suspends, expires, the number of active, suspected, and
dispatched requests, as well as meters of responses with ``1xx``, ``2xx``, ``3xx``, ``4xx``, and
``5xx`` status codes. It even has gauges for the ratios of ``4xx`` and ``5xx`` response rates to
overall response rates. Finally, it includes meters for requests by the HTTP method: ``GET``,
``POST``, etc.
