.. _manual-caffeine:

#####################
Instrumenting Caffeine
#####################

.. highlight:: text

.. rubric:: The ``metrics-caffeine`` module provides ``MetricsStatsCounter``, a metrics listener for
            Caffeine_ caches:

.. _Caffeine: https://github.com/ben-manes/caffeine

.. code-block:: java

    LoadingCache<Integer, Integer> cache = Caffeine.newBuilder()
        .recordStats(() -> new MetricsStatsCounter(registry, "cache"))
        .build(key -> key);

The listener publishes these metrics:

+---------------------------+----------------------------------------------------------------------+
| ``hits``                  | Number of times a requested item was found in the cache.             |
+---------------------------+----------------------------------------------------------------------+
| ``misses``                | Number of times a requested item was not found in the cache.         |
+---------------------------+----------------------------------------------------------------------+
| ``loads-success``         | Timer for successful loads into cache.                               |
+---------------------------+----------------------------------------------------------------------+
| ``loads-failure``         | Timer for failed loads into cache.                                   |
+---------------------------+----------------------------------------------------------------------+
| ``evictions``             | Histogram of eviction weights      .                                 |
+---------------------------+----------------------------------------------------------------------+
| ``evictions-weight``      | Total weight of evicted entries.                                     |
+---------------------------+----------------------------------------------------------------------+
| ``evictions.<CAUSE>``     | Histogram of eviction weights for each RemovalCause                  |
+---------------------------+----------------------------------------------------------------------+
