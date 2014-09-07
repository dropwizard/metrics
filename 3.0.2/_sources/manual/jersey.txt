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
