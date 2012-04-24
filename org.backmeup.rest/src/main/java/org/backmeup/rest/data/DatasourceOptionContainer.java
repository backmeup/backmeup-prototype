package org.backmeup.rest.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatasourceOptionContainer {
	private List<String> sourceOptions;

	public DatasourceOptionContainer(List<String> sourceOptions) {
		this.sourceOptions = sourceOptions;
	}

	public DatasourceOptionContainer() {
	}

	public List<String> getSourceOptions() {
		return sourceOptions;
	}

	public void setSourceOptions(List<String> sourceOptions) {
		this.sourceOptions = sourceOptions;
	}
}
