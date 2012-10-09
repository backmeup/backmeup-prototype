package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.BackMeUpUser;

@XmlRootElement
public class UserLoginContainer
{
	private boolean activated;

	public UserLoginContainer (BackMeUpUser user)
	{
		this.activated = user.isActivated ();
	}

	public boolean isActivated ()
	{
		return activated;
	}

	public void setActivated (boolean activated)
	{
		this.activated = activated;
	}
}
