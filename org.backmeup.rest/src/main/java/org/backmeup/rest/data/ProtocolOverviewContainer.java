package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.ProtocolOverview;

@XmlRootElement
public class ProtocolOverviewContainer {
	private ProtocolOverview overview;

	public ProtocolOverviewContainer() {
	}

	public ProtocolOverviewContainer(ProtocolOverview overview) {
		this.overview = overview;
	}

	public ProtocolOverview getOverview() {
		return overview;
	}

	public void setOverview(ProtocolOverview overview) {
		this.overview = overview;
	}
}
