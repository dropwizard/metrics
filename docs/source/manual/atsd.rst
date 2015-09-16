
.. _manual-atsd:

#####################
Reporting To ATSD
#####################

The ``metrics-atsd`` module implements ``AtsdReporter``, which allows Java applications to stream metrics into `Axibase Time-Series Database`__ .

.. __: https://axibase.com/products/axibase-time-series-database

#####################
Configure Sender
#####################

TCP:

.. code-block:: java

    final AtsdTCPSender sender = new AtsdTCPSender(new InetSocketAddress("atsd.example.com", 8081));

UDP:

.. code-block:: java

    final AtsdUDPSender sender = new AtsdUDPSender("atsd.example.com", 8082);

#####################
Builder Configuration
#####################
+--------------------------------------------------------------+-------------+-------------------------------+------------------------------------------------------------+
| **configuration**                                            | **requred** | **default**                   |                    **description**                         |
+--------------------------------------------------------------+-------------+-------------------------------+------------------------------------------------------------+
| ``public Builder setEntity(String entity)``                  | no          | *hostname* or "defaultEntity" |         Application name or hostname                       |
+--------------------------------------------------------------+-------------+-------------------------------+------------------------------------------------------------+
| ``public Builder withClock(Clock clock)``                    | no          | Clock.defaultClock()          |         Clock instance                                     |
+--------------------------------------------------------------+-------------+-------------------------------+------------------------------------------------------------+
| ``public Builder setMetricPrefix(String prefix)``            | no          | null                          |         Prefix metric names with the specified string      |
+--------------------------------------------------------------+-------------+-------------------------------+------------------------------------------------------------+
| ``public Builder convertRatesTo(TimeUnit rateUnit)``         | no          | TimeUnit.SECONDS              |         Convert rates to the specified period              |
+--------------------------------------------------------------+-------------+-------------------------------+------------------------------------------------------------+
| ``public Builder convertDurationsTo(TimeUnit durationUnit)`` | no          | TimeUnit.MILLISECONDS         |         Convert durations to the specified period          |
+--------------------------------------------------------------+-------------+-------------------------------+------------------------------------------------------------+
| ``public Builder filter(MetricFilter filter)``               | no          | MetricFilter.ALL              |         Only report metrics matching the specified filter  |
+--------------------------------------------------------------+-------------+-------------------------------+------------------------------------------------------------+
| ``public AtsdReporter build(AtsdSender sender)``             | yes         |                               |         Sending metrics using the specified AtsdSender     |
+--------------------------------------------------------------+-------------+-------------------------------+------------------------------------------------------------+

#####################
Add Metric
#####################

Add metric to monitor:

.. code-block:: java

    public class UserLogin {
        static final MetricRegistry registry = new MetricRegistry();
        static Meter meter = registry.meter(new MetricName("login.meter"));;
        ...
    }

Add metric to monitor with tags:

.. code-block:: java

    public class UserLogin {
        static final MetricRegistry registry = new MetricRegistry();
        static Meter meter = null;
        static {
            HashMap<String, String> tags = new HashMap();
            tags.put("provider", "ldap");
            meter = registry.meter(new MetricName("login.meter", tags));
        }
        ...
    }

#####################
Create Reporter
#####################

.. code-block:: java

    static void atsdTCPReport() {
        final AtsdTCPSender sender = new AtsdTCPSender(new InetSocketAddress("atsd.example.com", 8081));
        //final AtsdUDPSender sender = new AtsdUDPSender("atsd.example.com", 8082);
        final AtsdReporter reporter = AtsdReporter.forRegistry(metrics)
                .setEntity("portal-app")
                .prefixedWith("portal")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(sender);
        reporter.start(1, TimeUnit.SECONDS);
    }

#####################
Collect Metric Values
#####################

.. code-block:: java

    static void login() {
        meter.mark();
        System.out.println("method `login` was called!");
    }

#####################
Start Reporter
#####################

.. code-block:: java

    static void startReporter() {
        atsdTCPReport();
        //atsdUDPReport();
    }


######################
Series Output Example
######################

**Gauge**:

* series e:portal-app m:portal.login.gauge=5 ms:1442329089217

**Counter**:

* series e:portal-app m:portal.login.count.count=0 t:provider=ldap ms:1442329089217

**Histogram**:

* series e:portal-app m:portal.login.histogram.count=0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.max=0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.mean=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.min=0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.stddev=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.p50=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.p75=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.p95=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.p98=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.p99=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.histogram.p999=0.0 t:provider=ldap ms:1442329089217

**Meter**:

* series e:portal-app m:portal.login.meter.count=0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.meter.m1_rate=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.meter.m5_rate=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.meter.m15_rate=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.meter.mean_rate=0.0 t:provider=ldap ms:1442329089217

**Timer**:

* series e:portal-app m:portal.login.timer.max=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.mean=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.min=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.stddev=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.p50=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.p75=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.p95=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.p98=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.p99=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.p999=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.count=0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.m1_rate=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.m5_rate=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.m15_rate=0.0 t:provider=ldap ms:1442329089217
* series e:portal-app m:portal.login.timer.mean_rate=0.0 t:provider=ldap ms:1442329089217

#####################
Metrics
#####################

The library provides 5 types of metrics:

* Gauge_ : Current value.
* Counter_ : Incrementing and decrementing integer.
* Meter_ : Rate of events over time.
* Histogram_ : Statistical distribution of values.
* Timer_ : Rate at which the method is invoked and the distribution of its duration.

.. _Gauge: https://dropwizard.github.io/metrics/3.1.0/getting-started/#gauges
.. _Counter: https://dropwizard.github.io/metrics/3.1.0/getting-started/#counters
.. _Meter: https://dropwizard.github.io/metrics/3.1.0/getting-started/#meters
.. _Histogram: https://dropwizard.github.io/metrics/3.1.0/getting-started/#histograms
.. _Timer: https://dropwizard.github.io/metrics/3.1.0/getting-started/#timers