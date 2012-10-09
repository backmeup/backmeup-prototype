package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.backmeup.model.SearchResponse;
import org.backmeup.model.SearchResponse.CountedEntry;
import org.backmeup.model.SearchResponse.SearchEntry;

@XmlRootElement
public class SearchResponseContainer {	
	private List<SearchEntryContainer> files;
	private List<CountedEntryContainer> bySource;
	private List<CountedEntryContainer> byType;
	private int progress;

	public SearchResponseContainer() {
	}
	
	public SearchResponseContainer(SearchResponse resp) {
		this.files = new ArrayList<SearchEntryContainer>();
		for (SearchEntry entry : resp.getFiles())
			this.files.add(new SearchEntryContainer(entry));
		
		this.bySource = new ArrayList<CountedEntryContainer>();
		for (CountedEntry entry : resp.getBySource())
			this.bySource.add(new CountedEntryContainer(entry));
		
		this.byType = new ArrayList<CountedEntryContainer>();
		for (CountedEntry entry : resp.getByType())
			this.byType.add(new CountedEntryContainer(entry));

		this.progress = resp.getProgress();
	}
	
	public List<CountedEntryContainer> getBySource() {
		return bySource;
	}

	public void setBySource(List<CountedEntryContainer> bySource) {
		this.bySource = bySource;
	}

	public List<CountedEntryContainer> getByType() {
		return byType;
	}

	public void setByType(List<CountedEntryContainer> byType) {
		this.byType = byType;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public List<SearchEntryContainer> getFiles() {
		return files;
	}

	public void setFiles(List<SearchEntryContainer> files) {
		this.files = files;
	}
}
