package org.backmeup.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.backmeup.rest.data.ActionContainer;
import org.backmeup.rest.data.ActionOptionsContainer;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

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
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadPlugin(@FormDataParam("file") InputStream data, @FormDataParam("file") FormDataContentDisposition fileDetail ) throws IOException {
		getLogic().uploadActionPlugin(fileDetail.getFileName(), data);
		return Response.status(200).build();
	}
	
	@DELETE
	@Path("/{datasourceId}")
	public void deletePlugin(@PathParam("datasourceId") String source) {
		getLogic().deleteActionPlugin(source);
	}
}
