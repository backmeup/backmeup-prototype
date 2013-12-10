package org.backmeup.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

/**
 * This class contains search specific operations. 
 * 
 * @author fschoeppl
 *
 */
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
			@QueryParam("source") String source, @QueryParam("type") String type,
			@QueryParam("job") String job ) {
		SearchResponse sr = null;
		Map<String, List<String>> filters = null;
		
		if ((source != null) || (type != null) || (job != null))
		{
			filters = new HashMap<String, List<String>>();
			
			if (source != null)
			{
				List<String> filtervalue = new LinkedList<String>();
				filtervalue.add (source);
				filters.put ("source", filtervalue);
			}
			
			if (type != null)
			{
				List<String> filtervalue = new LinkedList<String>();
				filtervalue.add (type);
				filters.put ("type", filtervalue);
			}
			
			if (job != null)
			{
				List<String> filtervalue = new LinkedList<String>();
				filtervalue.add (job);
				filters.put ("job", filtervalue);
			}
			
		}
		
		sr = getLogic().queryBackup(username, searchId, filters);
		
		/*
		logger.debug("######################################################");
		logger.debug(sr.getQuery ());
		logger.debug(new SearchResponseContainer(sr).toString ());
		logger.debug("######################################################");
		*/
		
		return new SearchResponseContainer(sr);
	}
}
