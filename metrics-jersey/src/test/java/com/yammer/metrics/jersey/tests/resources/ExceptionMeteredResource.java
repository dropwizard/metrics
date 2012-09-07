package com.yammer.metrics.jersey.tests.resources;

import java.io.IOException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.yammer.metrics.annotation.ExceptionMetered;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
@ExceptionMetered(cause = IOException.class)
public class ExceptionMeteredResource {
	@GET
	@Path("/exception-metered-first")
	public String exceptionMeteredFirst(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException {
		if (splode) {
			throw new IOException("AUGH");
		}
		return "fuh";
	}

	@GET
	@Path("/exception-metered-second")
	public String exceptionMeteredSecond(@QueryParam("splode") @DefaultValue("false") boolean splode) throws IOException {
		if (splode) {
			throw new IOException("AUGH");
		}
		return "fuh";
	}

}
