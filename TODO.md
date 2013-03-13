Things What Need Doing
======================

* Batch gauges? Sets of gauges which share a common name prefix and pre-read method (e.g., JVM shiz)
* Get back up to feature parity w/ JVM instrumentation, EOL ``VirtualMachineMetrics``
* Figure out what configuring Jetty w/ instrumented stuff looks like (JNDI?)
* Figure out what configuring Ehcache w/ instrumented stuff looks like (JNDI?)
* Go through the docs with a fine-toothed comb and make sure things make sense
* Go through ``2.x-maintenance`` and make sure I didn't forget to forward-port something
* Add tests for ``JmxReporter``
* Add tests for ``JxmAttributeGauge``
* Add tests for ``LoggerReporter``
* Add tests and rate/duration units for ``ConsoleReporter``
* Add tests for ``CsvReporter``
* Go back and hit ``metrics-servlet`` with a stick
* Add javadocs for pretty much everything
* Do integration testing w/ Ganglia
* Do integration testing w/ Graphite

Do you see something here which is near and dear to your heart? WELL COME ON DOWN.
