.. _manual-third-party:

#####################
Third Party Libraries
#####################

If you're looking to integrate with something not provided by the main Metrics libraries, check out
the many third-party libraries which extend Metrics:

Instrumented Libraries
~~~~~~~~~~~~~~~~~~~~~~

* `camel-metrics <https://github.com/InitiumIo/camel-metrics>`_ provides component for your `Apache Camel <https://camel.apache.org/>`_ route.
* `hdrhistogram-metrics-reservoir <https://bitbucket.org/marshallpierce/hdrhistogram-metrics-reservoir>`_ provides a Histogram reservoir backed by `HdrHistogram <http://hdrhistogram.org/>`_.
* `jersey2-metrics <https://bitbucket.org/marshallpierce/jersey2-metrics>`_ provides integration with `Jersey 2 <https://jersey.java.net/>`_.
* `jersey-metrics-filter <https://github.com/palominolabs/jersey-metrics-filter>`_ provides integration with Jersey 1.
* `metrics-aspectj <https://github.com/astefanutti/metrics-aspectj>`_ provides integration with `AspectJ <http://eclipse.org/aspectj/>`_.
* `metrics-cdi <https://github.com/astefanutti/metrics-cdi>`_ provides integration with `CDI <http://www.cdi-spec.org/>`_ environments,
* `metrics-guice <https://github.com/palominolabs/metrics-guice>`_ provides integration with `Guice <https://code.google.com/p/google-guice/>`_.
* `metrics-guice-servlet <https://github.com/palominolabs/metrics-guice-servlet>`_ provides `Guice Servlet <https://github.com/google/guice/wiki/Servlets>`_ integration with AdminServlet.
* `metrics-okhttp <https://github.com/raskasa/metrics-okhttp>`_ provides integration with `OkHttp <http://square.github.io/okhttp>`_.
* `metrics-feign <https://github.com/mwiede/metrics-feign>`_ provides integration with `Feign <https://github.com/OpenFeign/feign>`_.
* `metrics-play <https://github.com/kenshoo/metrics-play>`_ provides an integration with the `Play Framework <https://www.playframework.com/>`_.
* `metrics-spring <https://github.com/ryantenney/metrics-spring>`_ provides integration with `Spring <http://spring.io/>`_.
* `wicket-metrics <https://github.com/NitorCreations/wicket-metrics>`_ provides easy integration for your `Wicket <http://wicket.apache.org/>`_ application.

Language Wrappers
~~~~~~~~~~~~~~~~~

* `metrics-clojure <https://github.com/sjl/metrics-clojure>`_ provides an API optimized for Clojure.
* `metrics-scala <https://github.com/erikvanoosten/metrics-scala>`_ provides an API optimized for Scala.

Reporters
~~~~~~~~~

* `finagle-metrics <https://github.com/rlazoti/finagle-metrics>`_ provides a reporter for a `Finagle <https://twitter.github.io/finagle/>`_ service.
* `kafka-dropwizard-metrics <https://github.com/SimpleFinance/kafka-dropwizard-reporter>`_ allows Kafka producers, consumers, and streaming applications to register their built-in metrics with a Dropwizard Metrics registry.
* `MetricCatcher <https://github.com/addthis/MetricCatcher>`_ Turns JSON over UDP into Metrics so that non-jvm languages can know what's going on too.
* `metrics-cassandra <https://github.com/brndnmtthws/metrics-cassandra>`_ provides a reporter for `Apache Cassandra <https://cassandra.apache.org/>`_.
* `metrics-circonus <https://github.com/circonus-labs/metrics-circonus>`_ provides a registry and reporter for sending metrics (including full histograms) to `Circonus <https://www.circonus.com/>`_.
* `metrics-datadog <https://github.com/coursera/metrics-datadog>`_ provides a reporter to send data to `Datadog <http://www.datadoghq.com/>`_.
* `metrics-elasticsearch-reporter <https://github.com/elasticsearch/elasticsearch-metrics-reporter-java>`_ provides a reporter for `elasticsearch <http://www.elasticsearch.org/>`_
* `metrics-hadoop-metrics2-reporter <https://github.com/joshelser/dropwizard-hadoop-metrics2>`_ provides a reporter for `Hadoop Metrics2 <https://hadoop.apache.org/docs/r2.7.2/api/org/apache/hadoop/metrics2/package-summary.html>`_.
* `metrics-hawkular <https://github.com/hawkular/hawkular-dropwizard-reporter>`_ provides a reporter for `Hawkular Metrics <http://www.hawkular.org/>`_.
* `metrics-influxdb <https://github.com/iZettle/dropwizard-metrics-influxdb>`_ provides a reporter for `InfluxDB <http://influxdb.org/>`_ with the Dropwizard framework integration.
* `metrics-influxdb <https://github.com/kickstarter/dropwizard-influxdb-reporter>`_ provides a reporter for `InfluxDB <http://influxdb.org/>`_ 1.2+._
* `metrics-instrumental <https://github.com/egineering-llc/metrics-instrumental>`_ provides a reporter to send data to `Instrumental <http://instrumentalapp.com/>`_.
* `metrics-kafka <https://github.com/hengyunabc/metrics-kafka>`_ provides a reporter for `Kafka <http://kafka.apache.org/>`_.
* `metrics-librato <https://github.com/librato/metrics-librato>`_ provides a reporter for `Librato Metrics <https://metrics.librato.com/>`_, a scalable metric collection, aggregation, monitoring, and alerting service.
* `metrics-mongodb-reporter <https://github.com/aparnachaudhary/mongodb-metrics-reporter>`_ provides a reporter for `MongoDB <https://www.mongodb.org/>`_.
* `metrics-munin-reporter <https://github.com/slashidea/metrics-munin-reporter>`_ provides a reporter for `Munin <http://munin-monitoring.org/>`_
* `metrics-new-relic <https://github.com/palominolabs/metrics-new-relic>`_ provides a reporter which sends data to `New Relic <http://newrelic.com/>`_.
* `metrics-reporter-config <https://github.com/addthis/metrics-reporter-config>`_ DropWizard-esque YAML configuration of reporters.
* `metrics-signalfx <https://github.com/signalfx/signalfx-java>`_ provides a reporter to send data to `SignalFx <http://www.signalfx.com/>`_.
* `metrics-spark-reporter <https://github.com/ippontech/metrics-spark-reporter>`_ provides a reporter for `Apache Spark Streaming <https://spark.apache.org/streaming/>`_.
* `metrics-splunk <https://github.com/zenmoto/metrics-splunk>`_ provides a reporter for `Splunk <http://www.splunk.com/>`_.
* `metrics-statsd <https://github.com/ReadyTalk/metrics-statsd>`_ provides a Metrics 2.x and 3.x reporter for `StatsD <https://github.com/etsy/statsd/>`_
* `metrics-zabbiz <https://github.com/hengyunabc/metrics-zabbix>`_ provides a reporter for `Zabbix <http://www.zabbix.com/>`_.
* `sematext-metrics-reporter <https://github.com/sematext/sematext-metrics-reporter>`_ provides a reporter for `SPM <http://sematext.com/spm/index.html>`_.

Advanced metrics implementations
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
* `rolling-metrics <https://github.com/vladimir-bukhtoyarov/rolling-metrics>`_ provides a collection of advanced metrics with rolling time window semantic, such as Rolling-Counter, Hit-Ratio, Top and Reservoir backed by HdrHistogram.
