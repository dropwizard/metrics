Metrics for Spring
==================

Enables the use of metrics-annotations with Spring, complete with simple XML configuration.

Getting Started
---------------

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:metrics="http://www.yammer.com/schema/metrics"
	xsi:schemaLocation="http://www.yammer.com/schema/metrics http://www.yammer.com/schema/metrics/metrics.xsd">

	<metrics:metrics-registry id="metrics" />
	<metrics:health-check-registry id="health" />

	<metrics:annotation-driven metrics-registry="metrics" health-check-registry="health" />

	<metrics:jmx-reporter id="metricsJmxReporter" metrics-registry="metrics" />

</beans>
```