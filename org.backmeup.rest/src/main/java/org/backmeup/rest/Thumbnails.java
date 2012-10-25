package org.backmeup.rest;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

@Path("/thumbnails")
public class Thumbnails extends Base {
	
	@GET
	@Path("{username}/{fileId}")
	@Produces("application/json")
	public Response getThumbnail(@PathParam("username") String username, @PathParam("fileId") String fileId) {
		File f = getLogic().getThumbnail(username, fileId);
		
		ResponseBuilder response;
		if (f == null) {
			response = Response.status(Status.NOT_FOUND);
		} else {
			response = Response.ok(f);
		}
		
		return response.build();
	}

}
