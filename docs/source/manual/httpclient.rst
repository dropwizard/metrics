.. _manual-httpclient:

###############################
Instrumenting Apache HttpClient
###############################

The ``metrics-httpclient`` module provides ``InstrumentedClientConnManager`` and
``InstrumentedHttpClient``, two instrumented versions of `Apache HttpClient 4.x`__ classes.

.. __: http://hc.apache.org/httpcomponents-client-ga/

``InstrumentedClientConnManager`` is a thread-safe ``ClientConnectionManager`` implementation which
measures the number of open connections in the pool and the rate at which new connections are
opened.

``InstrumentedHttpClient`` is a ``HttpClient`` implementation which has per-HTTP method timers for
HTTP requests.
