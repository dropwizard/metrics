.. _manual-servlet:

##############################
Instrumenting Web Applications
##############################

The ``metrics-servlet`` module provides a Servlet filter which has meters for status codes, a
counter for the number of active requests, and a timer for request duration. By default the filter
will use ``io.dropwizard.metrics5.servlet.InstrumentedFilter`` as the base name of the metrics.
You can use the filter in your ``web.xml`` like this:

.. code-block:: xml

    <filter>
        <filter-name>instrumentedFilter</filter-name>
        <filter-class>io.dropwizard.metrics5.servlet.InstrumentedFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>instrumentedFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>


An optional filter init-param ``name-prefix`` can be specified to override the base name
of the metrics associated with the filter mapping. This can be helpful if you need to instrument
multiple url patterns and give each a unique name.

.. code-block:: xml

    <filter>
        <filter-name>instrumentedFilter</filter-name>
        <filter-class>io.dropwizard.metrics5.servlet.InstrumentedFilter</filter-class>
        <init-param>
            <param-name>name-prefix</param-name>
            <param-value>authentication</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>instrumentedFilter</filter-name>
        <url-pattern>/auth/*</url-pattern>
    </filter-mapping>

You will need to add your ``MetricRegistry`` to the servlet context as an attribute named
``io.dropwizard.metrics5.servlet.InstrumentedFilter.registry``. You can do this using the Servlet API
by extending ``InstrumentedFilterContextListener``:

.. code-block:: java

    public class MyInstrumentedFilterContextListener extends InstrumentedFilterContextListener {
        public static final MetricRegistry REGISTRY = new MetricRegistry();

        @Override
        protected MetricRegistry getMetricRegistry() {
            return REGISTRY;
        }
    }
