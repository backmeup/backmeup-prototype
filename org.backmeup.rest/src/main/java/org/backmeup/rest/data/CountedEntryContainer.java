package org.backmeup.rest.data;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.SearchResponse.CountedEntry;

@XmlRootElement
public class CountedEntryContainer {
	
	private String title;
	private int count;
	
	public CountedEntryContainer(CountedEntry entry) {
		this.setTitle(entry.getTitle());
		this.setCount(entry.getCount());
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
