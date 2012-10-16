package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.ActionProfile.ActionProperty;

@XmlRootElement
public class ActionOptionsContainer {
	private List<String> actionOptions;	

	public List<String> getActionOptions() {
		return actionOptions;
	}

	public void setActionOptions(List<String> actionOptions) {
		this.actionOptions = actionOptions;
	}

	public ActionOptionsContainer() {
	}

	public ActionOptionsContainer(List<String> actionOptions) {
		this.actionOptions = actionOptions;
	}
	
	public ActionOptionsContainer(ActionProfile ap) {
	  this.actionOptions = new ArrayList<String>();
	  for (ActionProperty prop : ap.getActionOptions()) {
	    actionOptions.add(prop.getKey());
	  }
	}
}
