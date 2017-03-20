.. _manual-ehcache:

#####################
Instrumenting Ehcache
#####################

.. highlight:: text

.. rubric:: The ``metrics-ehcache`` module provides ``InstrumentedEhcache``, a decorator for
            Ehcache_ caches:

.. _Ehcache: http://ehcache.org/documentation

.. code-block:: java

    final Cache c = new Cache(new CacheConfiguration("test", 100));
    MANAGER.addCache(c);
    this.cache = InstrumentedEhcache.instrument(registry, c);

Instrumenting an ``Ehcache`` instance creates gauges for all of the Ehcache-provided statistics:

+---------------------------+----------------------------------------------------------------------+
| ``hits``                  | The number of times a requested item was found in the cache.         |
+---------------------------+----------------------------------------------------------------------+
| ``in-memory-hits``        | Number of times a requested item was found in the memory store.      |
+---------------------------+----------------------------------------------------------------------+
| ``off-heap-hits``         | Number of times a requested item was found in the off-heap store.    |
+---------------------------+----------------------------------------------------------------------+
| ``on-disk-hits``          | Number of times a requested item was found in the disk store.        |
+---------------------------+----------------------------------------------------------------------+
| ``misses``                | Number of times a requested item was not found in the cache.         |
+---------------------------+----------------------------------------------------------------------+
| ``in-memory-misses``      | Number of times a requested item was not found in the memory store.  |
+---------------------------+----------------------------------------------------------------------+
| ``off-heap-misses``       | Number of times a requested item was not found in the off-heap store.|
+---------------------------+----------------------------------------------------------------------+
| ``on-disk-misses``        | Number of times a requested item was not found in the disk store.    |
+---------------------------+----------------------------------------------------------------------+
| ``objects``               | Number of elements stored in the cache.                              |
+---------------------------+----------------------------------------------------------------------+
| ``in-memory-objects``     | Number of objects in the memory store.                               |
+---------------------------+----------------------------------------------------------------------+
| ``off-heap-objects``      | Number of objects in the off-heap store.                             |
+---------------------------+----------------------------------------------------------------------+
| ``on-disk-objects``       | Number of objects in the disk store.                                 |
+---------------------------+----------------------------------------------------------------------+
| ``mean-get-time``         | The average get time. Because ehcache supports JDK1.4.2, each get    |
|                           | time uses ``System.currentTimeMillis()``, rather than nanoseconds.   |
|                           | The accuracy is thus limited.                                        |
+---------------------------+----------------------------------------------------------------------+
| ``mean-search-time``      | The average execution time (in milliseconds) within the last sample  |
|                           | period.                                                              |
+---------------------------+----------------------------------------------------------------------+
| ``eviction-count``        | The number of cache evictions, since the cache was created, or       |
|                           | statistics were cleared.                                             |
+---------------------------+----------------------------------------------------------------------+
| ``searches-per-second``   | The number of search executions that have completed in the last      |
|                           | second.                                                              |
+---------------------------+----------------------------------------------------------------------+
| ``accuracy``              | A human readable description of the accuracy setting. One of "None", |
|                           | "Best Effort" or "Guaranteed".                                       |
+---------------------------+----------------------------------------------------------------------+

It also adds full timers for the cache's ``get`` and ``put`` methods.

The metrics are all scoped to the cache's class and name, so a ``Cache`` instance named ``users``
would have metric names like ``net.sf.ehcache.Cache.users.get``, etc.
