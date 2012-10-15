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

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.rest.data.JobContainer;
import org.backmeup.rest.data.JobCreationContainer;
import org.backmeup.rest.data.ProtocolDetailsContainer;
import org.backmeup.rest.data.ProtocolOverviewContainer;
import org.backmeup.rest.data.StatusContainer;
import org.backmeup.rest.data.ValidationNotesContainer;

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
	BackMeUpUser user = getLogic().getUser(username);
    return new JobContainer(getLogic().getJobs(username), user);
  }

  @POST
  @Path("/{username}/validate/{jobId}")
  @Produces("application/json")
  public ValidationNotesContainer validateBackupJob(
      @PathParam("username") String username,
      @PathParam("jobId") String jobId,
      @FormParam("keyRing") String keyRing) {
    return new ValidationNotesContainer(getLogic().validateBackupJob(username, Long.parseLong(jobId), keyRing));    
  }
  
  private Map<Long, String[]> mapOptions(List<String> sourceProfileIds, MultivaluedMap<String, String> formParams) {
    Map<Long, String[]> optionMapping = new HashMap<Long, String[]>();
    for (String sPId : sourceProfileIds) {
      Long id = Long.parseLong(sPId);
      List<String> options = formParams.get(sPId);
      optionMapping.put(id, options != null ? options.toArray(new String[] {}) : null); 
    }
    return optionMapping;
  }

  @POST
  @Path("/{username}")
  @Produces("application/json")
  public JobCreationContainer createBackupJob(
      @PathParam("username") String username,
      @FormParam("sourceProfileIds") List<String> sourceProfileIds,
      @FormParam("requiredActionIds") List<String> requiredActionIds,
      @FormParam("sinkProfileId") Long sinkProfileId,
      @FormParam("timeExpression") String timeExpression,
      @FormParam("keyRing") String keyRing,
      @FormParam("jobTitle") String jobTitle,
      MultivaluedMap<String, String> formParams) {
    // String cronTime, String keyRing);
    List<Long> sources = new ArrayList<Long>();
    for (String id : sourceProfileIds) {
      sources.add(Long.parseLong(id));
    }
    Map<Long, String[]> optionMapping = mapOptions(sourceProfileIds, formParams);
    BackupJob j = getLogic().createBackupJob(username, sources, sinkProfileId,
        optionMapping, requiredActionIds.toArray(new String[] {}),
        timeExpression, keyRing, jobTitle);
    return new JobCreationContainer(j.getId());
  }

  @DELETE
  @Path("/{username}/{jobId}")
  public void deleteJob(@PathParam("username") String username,
      @PathParam("jobId") Long jobId) {
    getLogic().deleteJob(username, jobId);
  }

  @GET
  @Path("/{username}/{jobId}/status")
  @Produces("application/json")
  public StatusContainer getStatus(@PathParam("username") String username,
      @PathParam("jobId") Long jobId) {
    
    return new StatusContainer(getLogic().getStatus(username, jobId));
  }

  @GET
  @Path("/{username}/{fileId}/details")
  @Produces("application/json")
  public ProtocolDetailsContainer getProtocolDetails(
      @PathParam("username") String username, @PathParam("fileId") String fileId) {
    return new ProtocolDetailsContainer(getLogic().getProtocolDetails(username,
        fileId));
  }

  @GET
  @Path("/{username}/status/overview")
  @Produces("application/json")
  public ProtocolOverviewContainer getProtocolOverview(
      @PathParam("username") String username,
      @QueryParam("duration") String duration) {
    if (duration == null)
      duration = "month";
    return new ProtocolOverviewContainer(getLogic().getProtocolOverview(
        username, duration));
  }

  @GET
  @Path("/{username}/status")
  @Produces("application/json")
  public StatusContainer getStatusWithoutJobId(
      @PathParam("username") String username,
      @QueryParam("fromDate") String fromDate,
      @QueryParam("toDate") String toDate) {
    Date fDate = null;
    Date tDate = null;
    if (fromDate != null) {
      fDate = new Date(Long.parseLong(fromDate));
    }
    if (toDate != null) {
      tDate = new Date(Long.parseLong(toDate));
    }
    return new StatusContainer(getLogic().getStatus(username, null));
  }
  
  @GET
  @Path("/{username}/{jobId}")
  @Produces("application/json")
  public StatusContainer getJobDetails(
      @PathParam("username") String username,
      @QueryParam("fromDate") String fromDate,
      @QueryParam("toDate") String toDate) {
    Date fDate = null;
    Date tDate = null;
    if (fromDate != null) {
      fDate = new Date(Long.parseLong(fromDate));
    }
    if (toDate != null) {
      tDate = new Date(Long.parseLong(toDate));
    }
    return new StatusContainer(getLogic().getStatus(username, null));
  }
}