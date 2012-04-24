package org.backmeup.rest.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatasinkContainer {
	private List<Datasink> sinks;

	public List<Datasink> getSinks() {
		return sinks;
	}


	public DatasinkContainer() {
	}
	
	public DatasinkContainer(List<Datasink> sinks) {
		super();
		this.sinks = sinks;
	}

	public void setSinks(List<Datasink> sinks) {
		this.sinks = sinks;
	}
	
	public static class Datasink {
		private String datasinkId;
		private String title;
		private String imageURL;
		private String description;
		
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDatasinkId() {
			return datasinkId;
		}

		public void setDatasinkId(String datasinkId) {
			this.datasinkId = datasinkId;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getImageURL() {
			return imageURL;
		}

		public void setImageURL(String imageURL) {
			this.imageURL = imageURL;
		}

		public Datasink() {
			
		}
		
		public Datasink(String datasinkId, String title, String imageURL, String description) {
			super();
			this.datasinkId = datasinkId;
			this.title = title;
			this.imageURL = imageURL;
			this.description = description;
		}
	}
}
