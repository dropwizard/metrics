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
