.. _manual-jersey:

########################
Instrumenting Jersey 2.x
########################

Jersey 2.x changed the API for how resource method monitoring works, so a new
module ``metrics-jersey2`` provides ``InstrumentedResourceMethodApplicationListener``,
which allows you to instrument methods on your `Jersey 2.x`_ resource classes:

The ``metrics-jersey2`` module provides ``InstrumentedResourceMethodApplicationListener``, which allows
you to instrument methods on your `Jersey 2.x`_ resource classes:

.. _Jersey 2.x: https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/index.html

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

        @GET
        @Metered(name = "fancyName")
        @Path("/metered")
        public String metered() {
            return "woo";
        }

        @GET
        @ExceptionMetered(cause = IOException.class)
        @Path("/exception-metered")
        public String exceptionMetered(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException {
            if (splode) {
                throw new IOException("AUGH");
            }
            return "fuh";
        }

        @GET
        @ResponseMetered
        @Path("/response-metered")
        public Response responseMetered(@QueryParam("invalid") @DefaultValue("false") boolean invalid) {
            if (invalid) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.ok().build();
        }
    }

Supported Annotations
=====================

Every resource method or the class itself can be annotated with @Timed, @Metered, @ResponseMetered and @ExceptionMetered.
If the annotation is placed on the class, it will apply to all its resource methods.

* ``@Timed`` adds a timer and measures time spent in that method.
* ``@Metered`` adds a meter and measures the rate at which the resource method is accessed.
* ``@ResponseMetered`` adds a meter and measures rate for each class of response codes (1xx/2xx/3xx/4xx/5xx).
* ``@ExceptionMetered`` adds a meter and measures how often the specified exception occurs when processing the resource.
  If the ``cause`` is not specified, the default is ``Exception.class``.
