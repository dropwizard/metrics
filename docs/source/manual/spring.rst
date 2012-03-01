.. _manual-spring:

##################
Spring Integration
##################

The ``metrics-spring`` module enables the use of Metrics annotations with
`Spring AOP`__, complete with simple XML configuration:

.. __: http://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/html/aop.html

.. code-block:: xml

    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:metrics="http://www.yammer.com/schema/metrics"
           xsi:schemaLocation="
               http://www.yammer.com/schema/metrics http://www.yammer.com/schema/metrics/metrics.xsd
               http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd">

        <metrics:metrics-registry id="metrics"/>
        <metrics:health-check-registry id="health"/>

        <metrics:annotation-driven metrics-registry="metrics" health-check-registry="health"/>

        <metrics:jmx-reporter id="metricsJmxReporter" metrics-registry="metrics"/>

        <!-- other beans -->

    </beans>

The most important part of the configuration is the element ``<metrics:annotation-driven />``.
This registers custom Spring bean processors, which serve three main purposes:

* Proxying beans which have methods annotated with ``@Timed``, ``@Metered``, and ``@ExceptionMetered``.
* Registering a ``Gauge`` for beans which have members annotated with ``@Gauge``.
* Registering beans which extend the class ``HealthCheck``.

The element accepts 5 optional arguments:

* ``metrics-registry``: the name of the ``MetricsRegsitry`` bean with which the generated metrics should be registered.
| If omitted, this defaults to registry provided by ``Metrics.defaultRegistry()``.
* ``health-check-registry``: the name of the ``HealthCheckRegsitry`` bean with which to register any beans which extend the class ``HealthCheck``.
| If omitted, this defaults to registry provided by ``HealthChecks.defaultRegistry()``.
* ``scope``: sets the scope for each of the metrics.
* ``proxy-target-class``: if set to ``true``, always creates CGLIB proxies instead of defaulting to JDK proxies. This is necessary if you use class-based autowiring.
* ``expose-proxy``: if set to ``true``, the target can access the proxy which wraps it by calling ``AopContext.currentProxy()``.

The elements ``<metrics:metrics-registry />`` and ``<metrics:health-check-registry />`` are present as a convenience for creating new registry beans.

The element ``<metrics:jmx-reporter />`` enables a JMX Reporter. If the attribute ``metrics-registry`` is omitted, the reporter is created with a reference to the registry provided by ``Metrics.defaultRegistry()``.
