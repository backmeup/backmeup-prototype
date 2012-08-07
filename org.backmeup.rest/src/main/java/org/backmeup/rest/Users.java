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

import org.backmeup.model.User;
import org.backmeup.model.exceptions.UnknownUserPropertyException;
import org.backmeup.rest.data.UserContainer;

/**
 * All user specific operations will be handled within this class.
 * 
 * @author fschoeppl
 *
 */
@Path("/users")
public class Users extends Base {	
	@GET
	@Path("{username}")
	@Produces("application/json")
	public UserContainer getUser(@PathParam("username") String username) {
		User user = getLogic().getUser(username);
		return new UserContainer(user.getUsername(), user.getEmail());
	}

	@DELETE
	@Path("{username}")
	@Produces("application/json")
	public void deleteUser(@PathParam("username") String username) {
		getLogic().deleteUser(username);
	}

	@PUT
	@Path("{username}")
	@Produces("application/json")
	public void changeUser(@PathParam("username") String username,
			@FormParam("oldPassword") String oldPassword,
			@FormParam("password") String newPassword,
			@FormParam("keyRing") String newKeyRing,
			@FormParam("email") String newEmail) {
		getLogic().changeUser(username, oldPassword, newPassword, newKeyRing, newEmail);
	}

	@POST
	@Path("{username}/login")
	@Produces("application/json")
	public void login(@PathParam("username") String username,
			@FormParam("password") String password) {
		getLogic().login(username, password);
	}
	
	@POST
	@Path("{username}/register")
	@Consumes({"application/x-www-form-urlencoded"})
	@Produces("application/json")
	public void register(@PathParam("username") String username,
			@FormParam("password") String password,
			@FormParam("keyRing") String keyRing,
			@FormParam("email") String email) {	  
	  getLogic().register(username, password,
				keyRing, email);
	}

	
	// User Properties
	@GET
	@Path("{username}/properties/{property}")
	@Produces("application/json")
	public String getUserProperty(@PathParam("username") String username,
	    @PathParam("property") String property
	    ) {
	  String result = getLogic().getUser(username).getUserProperty(property);
	  if (result == null)
	    throw new UnknownUserPropertyException(property);
	  return result;
	}
	
	@POST 
	@Path("{username}/properties/{property}/{value}")
  @Produces("application/json")
	public void setUserProperty(@PathParam("username") String username,
      @PathParam("property") String property,
      @PathParam("value") String value) {
    getLogic().setUserProperty(username, property, value);
  }
	
	@DELETE
	@Path("{username}/properties/{property}")
	@Produces("application/json")
	public void deleteUserProperty(@PathParam("username") String username, @PathParam("property") String property) {
	  getLogic().deleteUserProperty(username, property);
	}
}
