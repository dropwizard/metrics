package com.yammer.metrics.jersey.tests.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.yammer.metrics.annotation.Timed;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
@Timed
public class TimedResource {
	@GET
	@Path("/timed-first")
	public String timedFirst() {
		return "yay-first";
	}

	@GET
	@Path("/timed-second")
	public String timedSecond() {
		return "yay-second";
	}
}
