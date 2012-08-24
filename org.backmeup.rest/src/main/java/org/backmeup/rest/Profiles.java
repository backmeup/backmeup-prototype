package org.backmeup.rest;

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;

@Path("/profiles")
public class Profiles extends Base {
  
  @POST
  @Path("/{profileId}")
  public void updateProfileEntries(
      @PathParam("profileId") Long profileId,
      @FormParam("keyRing") String keyRing,
      MultivaluedMap<String, String> formParams) {
    Properties p = new Properties();
    for (Entry<String, List<String>> o : formParams.entrySet()) {
      String paramName = o.getKey();
      p.setProperty(paramName, o.getValue().get(0));
    }
    getLogic().addProfileEntries(profileId, p, keyRing);
  }
}
