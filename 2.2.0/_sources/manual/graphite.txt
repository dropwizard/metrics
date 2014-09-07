.. _manual-graphite:

#####################
Reporting to Graphite
#####################

The ``metrics-graphite`` module provides ``GraphiteReporter``, which allows your application to
constantly stream metric values to a Graphite_ server:

.. _Graphite: http://graphite.wikidot.com/

.. code-block:: java

    GraphiteReporter.enable(1, TimeUnit.MINUTES, "graphite.example.com", 2003);
