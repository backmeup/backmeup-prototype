package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.ProtocolDetails;

@XmlRootElement
public class ProtocolDetailsContainer {
	private  ProtocolDetails details;

	public ProtocolDetailsContainer() {
	}

	public ProtocolDetailsContainer(ProtocolDetails details) {
		this.details = details;
	}

	public ProtocolDetails getDetails() {
		return details;
	}

	public void setDetails(ProtocolDetails details) {
		this.details = details;
	}
}
