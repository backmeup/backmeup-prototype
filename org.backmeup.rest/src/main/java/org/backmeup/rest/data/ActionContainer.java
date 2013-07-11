package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.spi.ActionDescribable;

@XmlRootElement
public class ActionContainer {
	private List<ActionElement> actions;
	
	public ActionContainer() {
	}

	public ActionContainer(List<ActionDescribable> actions) {
		this.actions = new ArrayList<ActionElement>();
		for (ActionDescribable ad : actions) {
			this.actions.add(new ActionElement(ad.getTitle(), ad.getId(), ad.getDescription(), ad.getActionVisibility()));
		}
	}

	public List<ActionElement> getActions() {
		return actions;
	}

	public void setActions(List<ActionElement> actions) {
		this.actions = actions;
	}
	
	public static class ActionElement {
		private String title;
		private String actionId;
		private String description;
		private String visibility;

		public ActionElement() {
		}
		
		public ActionElement(String title, String actionId, String description, String visibility) {
			this.title = title;
			this.actionId = actionId;
			this.description = description;
			this.visibility = visibility;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getActionId() {
			return actionId;
		}

		public void setActionId(String actionId) {
			this.actionId = actionId;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

    public String getVisibility() {
      return visibility;
    }

    public void setVisibility(String visibility) {
      this.visibility = visibility;
    }
	}
}
