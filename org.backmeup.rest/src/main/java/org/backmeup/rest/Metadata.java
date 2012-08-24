package org.backmeup.rest;

import java.util.Properties;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.backmeup.rest.data.MetadataContainer;

@Path("/meta")
public class Metadata extends Base {
  @POST
  @Produces("application/json")
  @Path("/{username}/{profileId}/{property}")
  public MetadataContainer getSpecificMetadata(
      @PathParam("username") String username,
      @PathParam("profileId") String profileId,
      @PathParam("property") String property,
      @FormParam("keyRing") String keyRing) {
   Properties meta = getLogic().getMetadata(username, Long.parseLong(profileId), keyRing);
   MetadataContainer container = new MetadataContainer();
   container.setProperty(property, meta.getProperty(property));
   return container;
  }
  
  @POST
  @Produces("application/json")
  @Path("/{username}/{profileId}")
  public MetadataContainer getMetadata(
      @PathParam("username") String username,
      @PathParam("profileId") String profileId,
      @FormParam("keyRing") String keyRing) {
    Properties meta = getLogic().getMetadata(username, Long.parseLong(profileId), keyRing);
    MetadataContainer container = new MetadataContainer(meta);
    return container;
  }
}
