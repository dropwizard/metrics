package com.yammer.metrics.jersey.tests.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.yammer.metrics.annotation.Metered;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
@Metered
public class MeteredResource {
	@GET
	@Path("/metered-first")
	public String meteredFirst() {
		return "yay-first";
	}

	@GET
	@Path("/metered-second")
	public String meteredSecond() {
		return "yay-second";
	}
}
