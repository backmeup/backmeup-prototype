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
	@Path("/{username}/{fileId}")
	@Produces("image/jpeg")
	public Response getThumbnail(@PathParam("username") String username, @PathParam("fileId") String fileId) {
		System.out.println("Getting thumbnail for: " + username + ", " + fileId);
		File f = getLogic().getThumbnail(username, fileId);
				
		ResponseBuilder response;
		if (f == null) {
			System.out.println("Thumbnail not found - 404");
			response = Response.status(Status.NOT_FOUND);
		} else {
			System.out.println("Streaming back JPG file...");
			response = Response.ok(f);
		}
		
		return response.build();
	}

}
