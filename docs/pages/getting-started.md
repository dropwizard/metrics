Title: Getting Started
Template: manual.html.erb
Order: 2

* * *

The goal of this document is to guide you through the process of adding
Metrics to an existing application. We'll go through the various
measuring instruments that Metrics provides, how to use them, and when
they'll come in handy.

* * * * *

## Setting Up Maven

Just add the `metrics-core` library as a dependency:

``` xml
<dependencies>
    <dependency>
        <groupId>com.yammer.metrics</groupId>
        <artifactId>metrics-core</artifactId>
        <version>2.0.0-RC0</version>
    </dependency>
</dependencies>
```

Now it's time to add some metrics to your application!

* * * * *

## Gauges

A gauge is an instantaneous measurement of a value. For example, we may
want to measure the number of pending jobs in a queue.

``` java
Metrics.newGauge(QueueManager.class, "pending-jobs", new Gauge<Integer>() {
    @Override
    public Integer value() {
        return queue.size();
    }
});
```

Every time this gauge is measured, it will return the number of jobs in
the queue.

For most queue and queue-like structures, you won't want to simply
return `queue.size()`. Most of `java.util` and `java.util.concurrent`'s
implementations of `#size()` are `O(n)`, which means your gauge will be
slow (potentially while holding a lock).

* * * * *

## Counters

A counter is just a gauge for an `AtomicLong` instance. You can
increment or decrement its value. For example, we may want a more
efficient way of measuring the pending job in a queue:

``` java
private final Counter pendingJobs = Metrics.newCounter(QueueManager.class, "pending-jobs");

public void addJob(Job job) {
    pendingJobs.inc();
    queue.offer(job);
}

public Job takeJob() {
    pendingJobs.dec();
    return queue.take();
}
```

Every time this counter is measured, it will return the number of jobs
in the queue.

* * * * *

## Meters

A meter measures the rate of events over time (e.g., “requests per
second”). In addition to the mean rate, meters also track 1-, 5-, and
15-minute moving averages.

``` java
private final Meter requests = Metrics.newMeter(RequestHandler.class, "requests", "requests", TimeUnit.SECONDS);

public void handleRequest(Request request, Response response) {
    requests.mark();
    // etc
}
```

This meter will measure the rate of requests in requests per second.

* * * * *

## Histograms

A histogram measures the statistical distribution of values in a stream
of data. In addition to minimum, maximum, mean, etc., it also measures
median, 75th, 90th, 95th, 98th, 99th, and 99.9th percentiles.

``` java
private final Histogram responseSizes = Metrics.newHistogram(RequestHandler.class, "response-sizes");

public void handleRequest(Request request, Response response) {
    // etc
    responseSizes.update(response.getContent().length);
}
```

This histogram will measure the size of responses in bytes.

* * * * *

## Timers

A timer measures both the rate that a particular piece of code is called
and the distribution of its duration.

``` java
private final Timer responses = Metrics.newTimer(RequestHandler.class, "responses", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

public String handleRequest(Request request, Response response) {
    final TimerContext context = responses.time();
    try {
        // etc;
        return "OK";
    } finally {
        context.stop();
    }
}
```

This timer will measure the amount of time it takes to process each
request in milliseconds and provide a rate of requests in requests per
second.

* * * * *

## Health Checks

Metrics also has the ability to centralize your service's health checks.
First, implement a `HealthCheck` instance:

``` java
import com.yammer.metrics.core.HealthCheck.Result;

public class DatabaseHealthCheck extends HealthCheck {
    private final Database database;

    public DatabaseHealthCheck(Database database) {
        super("database");
        this.database = database;
    }

    @Override
    public Result check() throws Exception {
        if (database.isConnected()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Cannot connect to " + database.getUrl());
        }
    }
}
```

Then register an instance of it with Metrics:

``` java
HealthChecks.register(new DatabaseHealthCheck(database));
```

To run all of the registered health checks:

``` java
final Map<String, Result> results = HealthChecks.runHealthChecks();
for (Entry<String, Result> entry : results.entrySet()) {
    if (entry.getValue().isHealthy()) {
        System.out.println(entry.getKey() + " is healthy");
    } else {
        System.err.println(entry.getKey() + " is UNHEALTHY: " + entry.getValue().getMessage());
        final Throwable e = entry.getValue().getError();
        if (e != null) {
            e.printStackTrace();
        }
    }
}
```

Metrics comes with a pre-built health check: `DeadlockHealthCheck`,
which uses Java 1.6's built-in thread deadlock detection to determine if
any threads are deadlocked.

* * * * *

## Reporting Via JMX

All metrics are visible via **JConsole** or **VisualVM** (if you install
the JConsole plugin):

![JConsole
values](images/metrics-visualvm.png "Metrics exposed as JMX MBeans being viewed in VisualVM's MBeans browser")

**Bonus points**: If you double-click any of the metric properties,
VisualVM will start graphing the data for that property.

* * * * *

## Reporting Via HTTP

Metrics also ships with a servlet (`AdminServlet`) which will serve a
JSON representation of all registered metrics. It will also run health
checks, print out a thread dump, and provide a simple “ping” response
for load-balancers. (It also has single servlets—`MetricsServlet`,
`HealthCheckServlet`, `ThreadDumpServlet`, and `PingServlet`—which do
these individual tasks.)

To use this servlet, include the `metrics-servlet` module as a
dependency:

``` xml
<dependency>
    <groupId>com.yammer.metrics</groupId>
    <artifactId>metrics-servlet</artifactId>
    <version>2.0.0-RC0</version>
</dependency>
```

From there on, you can map the servlet to whatever path you see fit.

* * * * *

## Other Reporting

In addition to JMX and HTTP, Metrics also has reporters for the
following outputs:

* `STDOUT`, using `ConsoleReporter` from `metrics-core`
* `CSV` files, using `CsvReporter` from `metrics-core`
* Ganglia, using `GangliaReporter` from `metrics-ganglia`
* Graphite, using `GraphiteReporter` from `metrics-graphite`