% Overview

The Metrics project is broken up into a set of modules, each which a specific
purpose.

## Core Modules

These modules contain the basic classes that most Metrics-enabled applications
will use.

### `metrics-core`

`metrics-core` contains the basic metric classes:

* `GaugeMetric<T>`, which implements the functionality behind [gauges](gauges.html).
* `CounterMetric`, which implements the functionality behind [counters](counters.html).
* `MeterMetric`, which implements the functionality behind [meters](meters.html).
* `HistogramMetric`, which implements the functionality behind [histograms](histograms.html).
* `TimerMetric`, which implements the functionality behind [timers](timers.html).

It exports the values of all registered metrics via JMX, and optionally via the
console using `ConsoleReporter`. All other modules depend on `metrics-core`.

### `metrics-scala`

This module contains the Scala-specific façade that most Scala applications will
use: `Instrumented`.

### `metrics-servlet`

This module contains `MetricsServlet`, which provides a JSON representation of
all your applications' metrics as well as useful utility resources for running
health checks, establishing servlet context viability, gathering a thread dump,
etc.

## Integration Modules

### `metrics-ehcache`



### `metrics-graphite`

### `metrics-guice`

### `metrics-jetty`

### `metrics-log4j`

### `metrics-logback`
