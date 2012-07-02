package org.backmeup.rest;

import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;

@Path("/profiles")
public class Profiles extends Base {
  
  @PUT
  @Path("/{profileId}")
  public void updateProfileEntries(
      @PathParam("profileId") Long profileId,
      MultivaluedMap<String, String> formParams) {
    Properties p = new Properties();
    for (Entry<String, List<String>> o : formParams.entrySet()) {
      String paramName = o.getKey();
      p.setProperty(paramName, o.getValue().get(0));
    }
    getLogic().addProfileEntries(profileId, p);
  }
}
