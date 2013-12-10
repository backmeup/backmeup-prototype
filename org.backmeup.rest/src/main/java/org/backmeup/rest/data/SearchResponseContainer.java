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
	private List<CountedEntryContainer> byJob;
	private String searchQuery;
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
		
		this.byJob = new ArrayList<CountedEntryContainer>();
		for (CountedEntry entry : resp.getByJob())
			this.byJob.add(new CountedEntryContainer(entry));
		
		this.searchQuery = resp.getQuery();

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
	
	public List<CountedEntryContainer> getByJob ()
	{
		return byJob;
	}

	public void setByJob (List<CountedEntryContainer> byJob)
	{
		this.byJob = byJob;
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
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
	
	/*
	@Override
	public String toString ()
	{
		String retstr = "";
		for (SearchEntryContainer container : files)
		{
			try
			{
				retstr += Base64.encodeBytes ((container.getPreview ().replaceAll ("<em>","").replaceAll ("</em>", "")).getBytes ("UTF8")) + "\n";
				retstr += container.getPreview ().replaceAll ("<em>", "").replaceAll ("</em>", "") + "\n\n";
			}
			catch (Exception e)
			{
				logger.debug(e.toString ());
			}
		}
		
		return retstr;
	}
	*/
}
