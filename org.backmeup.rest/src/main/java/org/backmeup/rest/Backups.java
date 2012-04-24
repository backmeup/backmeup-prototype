package org.backmeup.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.backmeup.model.SearchResponse;
import org.backmeup.rest.data.BackupSearchContainer;
import org.backmeup.rest.data.SearchResponseContainer;

@Path("backups")
public class Backups extends Base {
	@Context
	UriInfo info;
	
	@POST
	@Path("/{username}/search")
	@Produces("application/json")
	public Response search(@PathParam("username") String username,
			@FormParam("keyRing") String keyRing,
			@FormParam("query") String query) throws URISyntaxException {
		long searchId = getLogic().searchBackup(username,
				keyRing, query);
		URI u = new URI(String.format("%sbackups/%s/%d/query", info
				.getBaseUri().toString(), username, searchId));
		return Response.status(Status.ACCEPTED).location(u)
				.entity(new BackupSearchContainer(searchId)).build();
	}

	@GET
	@Path("/{username}/{searchId}/query")
	@Produces("application/json")
	public SearchResponseContainer query(@PathParam("username") String username,
			@PathParam("searchId") Long searchId,
			@QueryParam("source") String source, @QueryParam("type") String type) {
		SearchResponse sr = null;

		if (source != null)
			sr = getLogic().queryBackup(username,
					searchId, "source", source);
		else if (type != null)
			sr = getLogic().queryBackup(username,
					searchId, "type", type);
		else 
			sr = getLogic().queryBackup(username,
					searchId, null, null);
		
		return new SearchResponseContainer(sr);
	}
}
