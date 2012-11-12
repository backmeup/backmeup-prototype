package org.backmeup.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.Profile;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.rest.data.DatasinkContainer;
import org.backmeup.rest.data.DatasinkContainer.Datasink;
import org.backmeup.rest.data.DatasinkProfilesContainer;
import org.backmeup.rest.data.PreAuthContainer;
import org.backmeup.rest.data.ResultMessage;
import org.backmeup.rest.data.ValidationNotesContainer;
import org.backmeup.rest.messages.Messages;

/**
 * All datasink specific operations will be handled within this class.
 * 
 * @author fschoeppl
 * 
 */
@Path("/datasinks")
public class Datasinks extends Base {

  @GET
  @Produces("application/json")
  public DatasinkContainer getDatasinks() {
    List<Datasink> l = new ArrayList<Datasink>();
    List<SourceSinkDescribable> descs = getLogic().getDatasinks();
    for (SourceSinkDescribable d : descs) {
      l.add(new Datasink(d.getId(), d.getTitle(), d.getImageURL(), d
          .getDescription()));
    }
    return new DatasinkContainer(l);
  }

  @GET
  @Path("/{username}/profiles")
  @Produces("application/json")
  public DatasinkProfilesContainer getDatasinkProfiles(
      @PathParam("username") String username) {
	BackMeUpUser user = getLogic().getUser(username);
    List<Profile> profiles = getLogic().getDatasinkProfiles(username);    
    return new DatasinkProfilesContainer(profiles, user);
  }

  @DELETE
  @Path("/{username}/profiles/{profileId}")
  public ResultMessage deleteProfile(@PathParam("username") String username,
      @PathParam("profileId") Long profileId) {
    getLogic().deleteProfile(username, profileId);
    return Messages.MSG_DELETE_SINK_PROFILE;
  }

  /*
   * @POST
   * 
   * @Consumes(MediaType.MULTIPART_FORM_DATA) public Response
   * uploadPlugin(@FormDataParam("file") InputStream data,
   * @FormDataParam("file") FormDataContentDisposition fileDetail) throws
   * IOException { getLogic().uploadDatasinkPlugin(fileDetail.getFileName(),
   * data); return Response.status(200).build(); }
   */

  @DELETE
  @Path("/{datasourceId}")
  public void deletePlugin(@PathParam("datasourceId") String source) {
    getLogic().deleteDatasinkPlugin(source);
  }

  @POST
  @Path("/{username}/{datasinkId}/auth")
  @Produces("application/json")
  public PreAuthContainer preAuthenticate(
      @PathParam("datasinkId") String datasinkId,
      @PathParam("username") String username,
      @FormParam("profileName") String profileName,
      @FormParam("keyRing") String keyRing) {
    AuthRequest ar = getLogic().preAuth(username, datasinkId, profileName,
        keyRing);
    return new PreAuthContainer(Long.toString(ar.getProfile().getProfileId()),
        ar.getRedirectURL() == null ? "Input" : "OAuth",
        ar.getRequiredInputs(), ar.getRedirectURL(), true);
  }

  @POST
  @Path("/{username}/validate/{profileId}")
  @Produces("application/json")
  public ValidationNotesContainer validateProfiles(
      @PathParam("username") String username, 
      @PathParam("profileId") Long profileId,
      @FormParam("keyRing") String keyRing) {
    return new ValidationNotesContainer(getLogic().validateProfile(username,
        profileId, keyRing));
  }

  @POST
  @Path("/{username}/{profileId}/auth/post")
  @Produces("application/json")
  public ResultMessage postAuthenticate(@PathParam("profileId") Long profileId,
      @PathParam("username") String username,
      @FormParam("keyRing") String keyRing,
      MultivaluedMap<String, String> formParams) {
    Properties p = new Properties();
    for (Entry<String, List<String>> o : formParams.entrySet()) {
      String paramName = o.getKey();
      if (!"profileId".equals(paramName) && !"keyRing".equals(paramName)) {
        p.setProperty(paramName, o.getValue().get(0));
      }
    }

    getLogic().postAuth(profileId, p, keyRing);
    return Messages.MSG_POST_AUTH_SINK_PROFILE;
  }
  
  @PUT
  @Path("/{username}/profiles/{profileId}/{jobId}")
  public void changeProfile (
		  @PathParam("username") String username,
		  @PathParam("profileId") Long profileId,
		  @PathParam("jobId") Long jobId,
		  List<String> formParams)
  {
	  getLogic ().changeProfile (profileId, jobId, formParams);
  }
}
