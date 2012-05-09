package org.backmeup.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.backmeup.model.BackupJob;
import org.backmeup.rest.data.JobContainer;
import org.backmeup.rest.data.JobCreationContainer;
import org.backmeup.rest.data.ProtocolDetailsContainer;
import org.backmeup.rest.data.ProtocolOverviewContainer;
import org.backmeup.rest.data.StatusContainer;

/**
 * All Job specific operations will be handled here.
 *  
 * @author fschoeppl
 *
 */
@Path("/jobs")
public class BackupJobs extends Base {

	@GET
	@Path("/{username}")
	@Produces("application/json")
	public JobContainer getJobs(@PathParam("username") String username) {
		return new JobContainer(getLogic().getJobs(username));
	}
	
	@POST
	@Path("/{username}")
	@Produces("application/json")
	public JobCreationContainer createBackupJob(@PathParam("username") String username,
			@FormParam("sourceProfileIds") List<String> sourceProfileIds,
			@FormParam("requiredActionIds") List<String> requiredActionIds,
			@FormParam("sinkProfileId") Long sinkProfileId,
			@FormParam("timeExpression") String timeExpression,
			@FormParam("keyRing") String keyRing,
			MultivaluedMap<String, String> formParams) {
		//String cronTime, String keyRing);
		List<Long> sources = new ArrayList<Long>();
		Map<Long, String[]> optionMapping = new HashMap<Long, String[]>();
		for (String sPId : sourceProfileIds) {
			Long id = Long.parseLong(sPId);
			List<String> options = formParams.get(sPId);
			if (options != null)
				optionMapping.put(id, options.toArray(new String[]{}));
			sources.add(id);
		}
		BackupJob j = getLogic().createBackupJob(username, sources, sinkProfileId, optionMapping, requiredActionIds.toArray(new String[]{}), timeExpression, keyRing);
		return new JobCreationContainer(j.getId());
	}
	
	@DELETE
	@Path("/{username}/{jobId}")
	public void deleteJob(@PathParam("username") String username, @PathParam("jobId") Long jobId) {
		getLogic().deleteJob(username, jobId);
	}
	
	@GET
	@Path("/{username}/{jobId}/status")
	@Produces("application/json")
	public StatusContainer getStatus(@PathParam("username") String username, @PathParam("jobId") Long jobId, @QueryParam("fromDate") String fromDate, @QueryParam("toDate") String toDate) {
		Date fDate = null;
		Date tDate = null;
		if (fromDate != null) {
			fDate = new Date(Long.parseLong(fromDate));
		}
		if (toDate != null) {
			tDate = new Date(Long.parseLong(toDate));
		}
		return new StatusContainer(getLogic().getStatus(username, jobId, fDate, tDate));
	}
	
	@GET
	@Path("/{username}/{fileId}/details")
	@Produces("application/json")
	public ProtocolDetailsContainer getProtocolDetails(@PathParam("username") String username, @PathParam("fileId") Long fileId) {
		return new ProtocolDetailsContainer(getLogic().getProtocolDetails(username, fileId));
	}
	
	@GET
	@Path("/{username}/status/overview")
	@Produces("application/json")
	public ProtocolOverviewContainer getProtocolOverview(@PathParam("username") String username, @QueryParam("duration") String duration) {
		if (duration == null)
			duration = "month";
		return new ProtocolOverviewContainer(getLogic().getProtocolOverview(username, duration));
	}
	
	@GET
	@Path("/{username}/status")
	@Produces("application/json")
	public StatusContainer getStatusWithoutJobId(@PathParam("username") String username, @QueryParam("fromDate") String fromDate, @QueryParam("toDate") String toDate) {
		Date fDate = null;
		Date tDate = null;
		if (fromDate != null) {
			fDate = new Date(Long.parseLong(fromDate));
		}
		if (toDate != null) {
			tDate = new Date(Long.parseLong(toDate));
		}
		return new StatusContainer(getLogic().getStatus(username, null, fDate, tDate));
	}
}