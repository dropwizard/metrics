.. _manual-jersey:

####################
Instrumenting Jersey
####################

The ``metrics-jersey`` module provides ``InstrumentedResourceMethodDispatchAdapter``, which allows
you to instrument methods on your Jersey_ resource classes:

.. _Jersey: http://jersey.java.net/

.. code-block:: java

    @Path("/example")
    @Produces(MediaType.TEXT_PLAIN)
    public class ExampleResource {
        @GET
        @Timed
        public String show() {
            return "yay";
        }
    }

The ``show`` method in the above example will have a timer attached to it, measuring the time spent
in that method.

Use of the ``@Metered`` and ``@ExceptionMetered`` annotations is also supported.

Your ``web.xml`` file will need to be modified to register ``InstrumentedResourceMethodDispatchAdapter`` as a Provider in Jersey_. 
This is done by adding ``com.yammer.metrics.jersey`` as the value for the ``com.sun.jersey.config.property.packages`` in ``init-param``.

.. code-block:: xml

	<servlet>
		<servlet-name>Test Servlet</servlet-name>
		<servlet-class>com.sun.jersey.spi.container.servlet.ServletContainer</servlet-class>
		<init-param>
			<param-name>com.sun.jersey.config.property.packages</param-name>
			<param-value>your.jersey.resources;com.yammer.metrics.jersey</param-value>
		</init-param>
	</servlet>