package com.codahale.metrics.jersey2.resources;

import javax.ws.rs.Path;

@Path("/child")
public class InstrumentedChildResource extends InstrumentedParentResource {
}
