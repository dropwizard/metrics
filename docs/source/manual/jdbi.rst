.. _manual-jdbi:

##################
Instrumenting JDBI
##################

The ``metrics-jdbi`` module provides a ``TimingCollector`` implementation for JDBI_, an SQL
convenience library.

.. _JDBI: http://jdbi.org/

To use it, just add a ``InstrumentedTimingCollector`` instance to your ``DBI``:

.. code-block:: java

    final DBI dbi = new DBI(dataSource);
    dbi.setTimingCollector(new InstrumentedTimingCollector());

``InstrumentedTimingCollector`` keeps per-SQL-object timing data, as well as general raw SQL timing
data.
