.. _manual-ganglia:

####################
Reporting to Ganglia
####################

The ``metrics-ganglia`` module provides ``GangliaReporter``, which allows your application to
constantly stream metric values to a Ganglia_ server:

.. _Ganglia: http://ganglia.sourceforge.net/

.. code-block:: java

    final GMetric ganglia = new GMetric("ganglia.example.com", 8649, UDPAddressingMode.MULTICAST, 1);
    final GangliaReporter reporter = GangliaReporter.forRegistry(registry)
                                                    .convertRatesTo(TimeUnit.SECONDS)
                                                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                    .build(ganglia);
    reporter.start(1, TimeUnit.MINUTES);
