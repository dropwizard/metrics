.. _manual-ganglia:

####################
Reporting to Ganglia
####################

The ``metrics-ganglia`` module provides ``GangliaReporter``, which allows your application to
constantly stream metric values to a Ganglia_ server:

.. _Ganglia: http://ganglia.sourceforge.net/

.. code-block:: java

    GangliaReporter.enable(1, TimeUnit.MINUTES, "ganglia.example.com", 8649);
