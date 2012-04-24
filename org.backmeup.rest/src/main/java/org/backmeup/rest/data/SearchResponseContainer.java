package org.backmeup.rest.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.SearchResponse;
import org.backmeup.model.SearchResponse.CountedEntry;
import org.backmeup.model.SearchResponse.SearchEntry;

@XmlRootElement
public class SearchResponseContainer {	
	private List<SearchEntry> files;
	private List<CountedEntry> bySource;
	private List<CountedEntry> byType;
	private int progress;

	public SearchResponseContainer() {
	}
	
	public SearchResponseContainer(SearchResponse resp) {
		this.files = resp.getFiles();
		this.progress = resp.getProgress();
		this.bySource = resp.getBySource();
		this.byType = resp.getByType();
	}
	
	public List<CountedEntry> getBySource() {
		return bySource;
	}

	public void setBySource(List<CountedEntry> bySource) {
		this.bySource = bySource;
	}

	public List<CountedEntry> getByType() {
		return byType;
	}

	public void setByType(List<CountedEntry> byType) {
		this.byType = byType;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public List<SearchEntry> getFiles() {
		return files;
	}

	public void setFiles(List<SearchEntry> files) {
		this.files = files;
	}
}
