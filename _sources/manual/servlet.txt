.. _manual-servlet:

##############################
Instrumenting Web Applications
##############################

The ``metrics-servlet`` module provides a Servlet filter which has meters for status codes, a
counter for the number of active requests, and a timer for request duration. You can use it in your
``web.xml`` like this:

.. code-block:: xml

    <filter>
        <filter-name>instrumentedFilter</filter-name>
        <filter-class>com.codahale.metrics.servlet.InstrumentedFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>instrumentedFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

You will need to add your ``MetricRegistry`` to the servlet context as an attribute named
``com.codahale.metrics.servlet.InstrumentedFilter.registry``. You can do this using the Servlet API
by extending ``InstrumentedFilterContextListener``:

.. code-block:: java

    public class MyInstrumentedFilterContextListener extends InstrumentedFilterContextListener {
        public static final MetricRegistry REGISTRY = new MetricRegistry();

        @Override
        protected MetricRegistry getMetricRegistry() {
            return REGISTRY;
        }
    }
