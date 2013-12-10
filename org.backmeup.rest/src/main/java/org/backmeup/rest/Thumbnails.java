package org.backmeup.rest;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/thumbnails")
public class Thumbnails extends Base {
	private final Logger logger = LoggerFactory.getLogger(Thumbnails.class);
	
	@GET
	@Path("/{username}/{fileId}")
	@Produces("image/jpeg")
	public Response getThumbnail(@PathParam("username") String username, @PathParam("fileId") String fileId) {
		logger.debug("Getting thumbnail for: " + username + ", " + fileId);
			
		ResponseBuilder response;
		try {
			File f = getLogic().getThumbnail(username, fileId);
			if (f == null) {
				logger.debug("Thumbnail not found - 404");
				response = Response.status(Status.NOT_FOUND);
			} else {
				logger.debug("Streaming back JPG file...");
				response = Response.ok(f);
			}
		} catch (Throwable t) {
			response = Response.status(Status.NOT_FOUND);
		}
		
		return response.build();
	}

}
