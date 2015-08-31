package com.codahale.metrics.jersey2.resources;

import javax.ws.rs.Path;

@Path("/secondchild")
public class InstrumentedSecondChildResource extends InstrumentedParentResource {
}
