package org.backmeup.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.exceptions.UnknownUserPropertyException;
import org.backmeup.rest.data.ResultMessage;
import org.backmeup.rest.data.ResultMessage.Type;
import org.backmeup.rest.data.UserContainer;
import org.backmeup.rest.data.UserLoginContainer;
import org.backmeup.rest.data.VerificationContainer;
import org.backmeup.rest.data.VerifyEmailContainer;
import org.backmeup.rest.messages.Messages;

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
		BackMeUpUser user = getLogic().getUser(username);
		return new UserContainer(user);
	}

	@DELETE
	@Path("{username}")
	@Produces("application/json")
	public ResultMessage deleteUser(@PathParam("username") String username) {
		getLogic().deleteUser(username);
		return Messages.MSG_DELETE_USER;
	}

	@PUT
	@Path("{oldUsername}")
	@Produces("application/json")
	public ResultMessage changeUser(@PathParam("oldUsername") String oldUsername,
	    @FormParam("username") String username,
			@FormParam("oldPassword") String oldPassword,
			@FormParam("password") String newPassword,
			@FormParam("oldKeyring") String oldKeyring,
			@FormParam("newKeyring") String newKeyring,
			@FormParam("email") String newEmail) {
	  BackMeUpUser oldUser = getLogic().getUser(oldUsername);
		BackMeUpUser user = getLogic().changeUser(oldUsername, username, oldPassword, newPassword, oldKeyring, newKeyring, newEmail);
		ResultMessage rslt = new ResultMessage(Type.success);
		if (!oldUsername.equals(user.getUsername())) {
		  rslt.addMessage(Messages.CHANGE_USER_USERNAME);
		}
		if (newPassword != null && !oldPassword.equals(newPassword)) {
		  rslt.addMessage(Messages.CHANGE_USER_PASSWORD);
    }
		if (newEmail != null && !oldUser.getEmail().equals(newEmail)) {
		  rslt.addMessage(Messages.CHANGE_USER_EMAIL);
		}
		return rslt;
	}

	@POST
	@Path("{username}/login")
	@Produces("application/json")
	public UserLoginContainer login(@PathParam("username") String username,
			@FormParam("password") String password) {
		return new UserLoginContainer (getLogic().login(username, password));
	}
	
	@POST
	@Path("{username}/register")
	@Consumes({"application/x-www-form-urlencoded"})
	@Produces("application/json")
	public VerificationContainer register(@PathParam("username") String username,
			@FormParam("password") String password,
			@FormParam("keyRing") String keyRing,
			@FormParam("email") String email) {	  
	  BackMeUpUser u = getLogic().register(username, password,
				keyRing, email);
	  return new VerificationContainer(u.getUsername(), u.getVerificationKey());
	}
	
	@GET
	@Path("{verificationKey}/verifyEmail")
	@Produces("application/json")
	public VerifyEmailContainer verifyEmailAddress(@PathParam("verificationKey") String verificationKey) {
	  return new VerifyEmailContainer(getLogic().verifyEmailAddress(verificationKey));
	}
	
	@GET
	@Path("{username}/deleteIndex")
	@Produces("application/json")
	public ResultMessage deleteIndex(@PathParam("username") String username) {
		getLogic().deleteIndexForUser(username);
		return Messages.MSG_DELETE_INDEX_FOR_USER;
	}
	
	@GET
	@Path("{username}/newVerificationEmail")
	@Produces("application/json")
	public VerificationContainer requestNewVerificationEmail(@PathParam("username") String username) {
	  BackMeUpUser u = getLogic().requestNewVerificationEmail(username);
	  return new VerificationContainer(u.getUsername(), u.getVerificationKey());
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
	public ResultMessage setUserProperty(@PathParam("username") String username,
      @PathParam("property") String property,
      @PathParam("value") String value) {
    getLogic().setUserProperty(username, property, value);
    return Messages.MSG_POST_PROPERTY;
  }
	
	@DELETE
	@Path("{username}/properties/{property}")
	@Produces("application/json")
	public ResultMessage deleteUserProperty(@PathParam("username") String username, @PathParam("property") String property) {
	  getLogic().deleteUserProperty(username, property);
	  return Messages.MSG_DELETE_USER_PROPERTY;
	}
	
	@GET
	@Path("{username}/keysrvlogs")
	@Produces("application/json")
	public List<KeyserverLog> getUserKeysrvlogs(@PathParam("username") String username)
	{
	  BackMeUpUser user = getLogic().getUser (username);
	  return getLogic ().getKeysrvLogs (user);
	}
}
