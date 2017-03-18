.. _manual-graphite:

#####################
Reporting to Graphite
#####################

The ``metrics-graphite`` module provides ``GraphiteReporter``, which allows your application to
constantly stream metric values to a Graphite_ server:

.. _Graphite: http://graphite.wikidot.com/

.. code-block:: java

    final Graphite graphite = new Graphite(new InetSocketAddress("graphite.example.com", 2003));
    final GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
                                                      .prefixedWith("web1.example.com")
                                                      .convertRatesTo(TimeUnit.SECONDS)
                                                      .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                      .filter(MetricFilter.ALL)
                                                      .build(graphite);
    reporter.start(1, TimeUnit.MINUTES);

If you prefer to write metrics in batches using pickle, you can use the ``PickledGraphite``:

.. code-block:: java

    final PickledGraphite pickledGraphite = new PickledGraphite(new InetSocketAddress("graphite.example.com", 2004));
    final GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
                                                      .prefixedWith("web1.example.com")
                                                      .convertRatesTo(TimeUnit.SECONDS)
                                                      .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                      .filter(MetricFilter.ALL)
                                                      .build(pickledGraphite);
    reporter.start(1, TimeUnit.MINUTES);
