package org.backmeup.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

import org.backmeup.rest.data.ActionContainer;
import org.backmeup.rest.data.ActionOptionsContainer;

/**
 * This class provides all action specific operations,
 * delegating them to the business logic.
 * 
 * @author fschoeppl
 *
 */
@Path("/actions")
public class Actions extends Base {		 
	@GET
	@Produces("application/json")
	public ActionContainer getActions() {
		return new ActionContainer(getLogic().getActions());
	}
	
	@GET
	@Produces("application/json")
	@Path("/{actionId}/options")
	public ActionOptionsContainer getActionOptions(@PathParam("actionId") String actionId) {
		return new ActionOptionsContainer(getLogic().getActionOptions(actionId));
	}
	
	@GET
  @Produces("application/json")
  @Path("/{actionId}/storedOptions/{jobId}")
  public ActionOptionsContainer getStoredActionOptions(@PathParam("actionId") String actionId,
      @PathParam("jobId") Long jobId) {
    return new ActionOptionsContainer(getLogic().getStoredActionOptions(actionId, jobId));
  }
	
	@PUT
  @Path("/{actionId}/options/{jobId}")
  @Produces("application/json")
  public void updateActionOptions(      
      @PathParam("actionId") String actionId,
      @PathParam("jobId") Long jobId,
      MultivaluedMap<String, String> formParams) {
	  Map<String, String> actionOptions = new HashMap<String, String>();
	  for (Entry<String, List<String>> entry : formParams.entrySet()) {
	    actionOptions.put(entry.getKey(), entry.getValue().get(0));
	  }
	  getLogic().changeActionOptions(actionId, jobId, actionOptions);       
  }
	
	/*@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadPlugin(@FormDataParam("file") InputStream data, @FormDataParam("file") FormDataContentDisposition fileDetail ) throws IOException {
		getLogic().uploadActionPlugin(fileDetail.getFileName(), data);
		return Response.status(200).build();
	}*/
	
	@DELETE
	@Path("/{datasourceId}")
	public void deletePlugin(@PathParam("datasourceId") String source) {
		getLogic().deleteActionPlugin(source);
	}
}
