package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.rest.messages.Messages;

@XmlRootElement
public class UserLoginContainer
{
	private boolean activated;
	private String type = "success";
	private String message = Messages.LOGIN_USER;
	private Long userId;

	public String getType() {
    return type;
  }
  
  public String getMessage() {
    return message;
  }

  public UserLoginContainer (BackMeUpUser user)
	{
		this.activated = user.isActivated ();
		this.userId = user.getUserId();
	}

	public boolean isActivated ()
	{
		return activated;
	}

	public void setActivated (boolean activated)
	{
		this.activated = activated;
	}

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
