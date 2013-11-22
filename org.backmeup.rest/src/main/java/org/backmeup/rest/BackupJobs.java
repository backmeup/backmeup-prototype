package org.backmeup.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.dto.JobCreationRequest;
import org.backmeup.model.dto.JobUpdateRequest;
import org.backmeup.rest.data.JobContainer;
import org.backmeup.rest.data.ProtocolDetailsContainer;
import org.backmeup.rest.data.ProtocolOverviewContainer;
import org.backmeup.rest.data.ResultMessage;
import org.backmeup.rest.data.StatusContainer;
import org.backmeup.rest.data.ValidationNotesContainer;
import org.backmeup.rest.messages.Messages;
import org.backmeup.rest.parsers.JobCreationRequestParser;

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
  
  @POST
  @Path("/{username}")
  @Produces("application/json")
  @Consumes("application/x-www-form-urlencoded")
  public ValidationNotesContainer createBackupJob(@PathParam("username") String username, MultivaluedMap<String, String> validationRequest) {
    return createBackupJob(username, JobCreationRequestParser.parse(validationRequest));
  }

  @POST
  @Path("/{username}")
  @Produces("application/json")
  public ValidationNotesContainer createBackupJob(@PathParam("username") String username, JobCreationRequest request) {    
    return new ValidationNotesContainer(getLogic().createBackupJob(username, request));
  }
  
  @PUT
  @Path("/{username}/{jobId}")
  @Produces("application/json")
  @Consumes("application/x-www-form-urlencoded")
  public ValidationNotesContainer updateBackupJob(@PathParam("username") String username, @PathParam("jobId") Long jobId, MultivaluedMap<String, String> validationRequest) {
    JobUpdateRequest jur = JobCreationRequestParser.parseUpdateRequest(validationRequest);    
    return updateBackupJob(username, jobId, jur);
  }

  @PUT
  @Path("/{username}")
  @Produces("application/json")
  public ValidationNotesContainer updateBackupJob(@PathParam("username") String username, @PathParam("jobId") Long jobId, JobUpdateRequest request) {
    request.setJobId(jobId);
    return new ValidationNotesContainer(getLogic().updateBackupJob(username, request));
  }
  
  @GET
  @Path("/{username}/{jobId}")
  @Produces("application/json")
  public JobUpdateRequest getBackupJob(@PathParam("username") String username, @PathParam("jobId") Long jobId) {
    return getLogic().getBackupJob(username, jobId); 
  }

  @DELETE
  @Path("/{username}/{jobId}")
  public ResultMessage deleteJob(@PathParam("username") String username,
      @PathParam("jobId") Long jobId) {
    getLogic().deleteJob(username, jobId);
    return Messages.MSG_DELETE_JOB;
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
    return new StatusContainer(getLogic().getStatus(username, null));
  }
  
  /*@GET
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
  }*/
}