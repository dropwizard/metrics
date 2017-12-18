.. _manual-collectd:

#####################
Reporting to Collectd
#####################

The ``metrics-collectd`` module provides ``CollectdReporter``, which allows your application to
constantly stream metric values to a Collectd_ server:

.. _Collectd: https://collectd.org/

.. code-block:: java

    final Collectd collectd = new Collectd(new InetSocketAddress("collectd.example.com", 2007));
    final CollectdReporter reporter = CollectdReporter.forRegistry(registry)
                                                      .convertRatesTo(TimeUnit.SECONDS)
                                                      .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                      .filter(MetricFilter.ALL)
                                                      .build(collectd);
    reporter.start(1, TimeUnit.MINUTES);

