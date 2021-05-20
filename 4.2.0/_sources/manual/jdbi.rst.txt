.. _manual-jdbi:

##################
Instrumenting JDBI
##################

The ``metrics-jdbi`` and ``metrics-jdbi3`` modules provide a ``TimingCollector`` implementation for JDBI_, an SQL
convenience library.

.. _JDBI: http://jdbi.org/

To use it, just add a ``InstrumentedTimingCollector`` instance to your ``DBI``:

.. code-block:: java

    final DBI dbi = new DBI(dataSource);
    dbi.setTimingCollector(new InstrumentedTimingCollector(registry));

``InstrumentedTimingCollector`` keeps per-SQL-object timing data, as well as general raw SQL timing
data. The metric names for each query are constructed by an ``StatementNameStrategy`` instance, of
which there are many implementations. By default, ``StatementNameStrategy`` uses
``SmartNameStrategy``, which attempts to effectively handle both queries from bound objects and raw
SQL.
