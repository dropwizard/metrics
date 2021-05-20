.. _manual-httpclient:

###############################
Instrumenting Apache HttpClient
###############################

The ``metrics-httpclient`` module provides ``InstrumentedHttpClientConnManager`` and
``InstrumentedHttpClients``, two instrumented versions of `Apache HttpClient 4.x`__ classes.

.. __: http://hc.apache.org/httpcomponents-client-ga/

``InstrumentedHttpClientConnManager`` is a thread-safe ``HttpClientConnectionManager`` implementation which
measures the number of open connections in the pool and the rate at which new connections are
opened.

``InstrumentedHttpClients`` follows the ``HttpClients`` builder pattern and adds per-HTTP method timers for
HTTP requests.


Metric naming strategies
========================
The default per-method metric naming and scoping strategy can be overridden by passing an
implementation of ``HttpClientMetricNameStrategy`` to the ``InstrumentedHttpClients.createDefault`` method.

A number of pre-rolled strategies are available, e.g.:

.. code-block:: java

    HttpClient client = InstrumentedHttpClients.createDefault(registry, HttpClientMetricNameStrategies.HOST_AND_METHOD);
