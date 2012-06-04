package org.backmeup.rest;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.backmeup.rest.data.MetadataContainer;

@Path("/meta")
public class Metadata extends Base {
  @GET
  @Produces("application/json")
  @Path("/{username}/{profileId}/{property}")
  public MetadataContainer getSpecificMetadata(
      @PathParam("username") String username,
      @PathParam("profileId") String profileId,
      @PathParam("property") String property) {
   Properties meta = getLogic().getMetadata(username, Long.parseLong(profileId));
   MetadataContainer container = new MetadataContainer();
   container.setProperty(property, meta.getProperty(property));
   return container;
  }
  
  @GET
  @Produces("application/json")
  @Path("/{username}/{profileId}")
  public MetadataContainer getMetadata(
      @PathParam("username") String username,
      @PathParam("profileId") String profileId) {
    Properties meta = getLogic().getMetadata(username, Long.parseLong(profileId));
    MetadataContainer container = new MetadataContainer(meta);
    return container;
  }
}
