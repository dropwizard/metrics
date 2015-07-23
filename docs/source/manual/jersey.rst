.. _manual-jersey:

########################
Instrumenting Jersey 2.x
########################

Jersey 2.x changed the API for how resource method monitoring works, so a new
module ``metrics-jersey2`` provides ``InstrumentedResourceMethodApplicationListener``,
which allows you to instrument methods on your `Jersey 2.x`_ resource classes:

The ``metrics-jersey2`` module provides ``InstrumentedResourceMethodApplicationListener``, which allows
you to instrument methods on your `Jersey 2.x`_ resource classes:

.. _Jersey 2.x: https://jersey.java.net/documentation/latest/index.html

An instance of ``InstrumentedResourceMethodApplicationListener`` must be registered with your Jersey
application's ``ResourceConfig`` as a singleton provider for this to work.

.. code-block:: java

    public class ExampleApplication extends ResourceConfig {
        .
        .
        .
        register(new InstrumentedResourceMethodApplicationListener (new MetricRegistry()));
        config = config.register(ExampleResource.class);
        .
        .
        .
    }

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
