.. _manual-jvm:

###################
JVM Instrumentation
###################

The ``metrics-jvm`` module contains a number of reusable gauges and
:ref:`metric sets <man-core-sets>` which allow you to easily instrument JVM internals.

Supported metrics include:

* Run count and elapsed times for all supported garbage collectors
* Memory usage for all memory pools, including off-heap memory
* Breakdown of thread states, including deadlocks
* File descriptor usage
* Buffer pool sizes and utilization
