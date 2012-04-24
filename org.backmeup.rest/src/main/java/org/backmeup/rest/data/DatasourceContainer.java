package org.backmeup.rest.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatasourceContainer {
	private List<Datasource> sources;

	public List<Datasource> getSources() {
		return sources;
	}


	public DatasourceContainer() {
	}
	
	public DatasourceContainer(List<Datasource> sources) {
		super();
		this.sources = sources;
	}

	public void setSources(List<Datasource> sources) {
		this.sources = sources;
	}
	
	public static class Datasource {
		private String datasourceId;
		private String title;
		private String imageURL;
		
		public String getDatasourceId() {
			return datasourceId;
		}

		public void setDatasourceId(String datasourceId) {
			this.datasourceId = datasourceId;
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

		public Datasource() {
			
		}
		
		public Datasource(String datasourceId, String title, String imageURL) {
			super();
			this.datasourceId = datasourceId;
			this.title = title;
			this.imageURL = imageURL;
		}
	}
}
