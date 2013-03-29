.. _manual-servlet:

##############################
Instrumenting Web Applications
##############################

The ``metrics-servlet`` module provides a Servlet filter which has meters for status codes, a counter
for the number of active requests, and a timer for request duration. You can use it in your
``web.xml`` like this:

.. code-block:: xml

    <filter>
        <filter-name>webappMetricsFilter</filter-name>
        <filter-class>com.yammer.metrics.servlet.DefaultWebappMetricsFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>webappMetricsFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
