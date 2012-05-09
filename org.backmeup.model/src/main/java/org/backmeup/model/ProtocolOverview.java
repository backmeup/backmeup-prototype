package org.backmeup.model;

import java.util.List;

/**
 * This class contains a protocol overview
 * about a certain user.
 * 
 * @author fschoeppl
 *
 */
public class ProtocolOverview {
	private String totalCount;
	private String totalStored;
	private List<Entry> storedAmount;
	private List<Entry> datasinks;

	public ProtocolOverview() {
	}

	public ProtocolOverview(String totalCount, String totalStored,
			List<Entry> storedAmount, List<Entry> datasinks) {
		this.totalCount = totalCount;
		this.totalStored = totalStored;
		this.storedAmount = storedAmount;
		this.datasinks = datasinks;
	}

	public String getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(String totalCount) {
		this.totalCount = totalCount;
	}

	public String getTotalStored() {
		return totalStored;
	}

	public void setTotalStored(String totalStored) {
		this.totalStored = totalStored;
	}

	public List<Entry> getStoredAmount() {
		return storedAmount;
	}

	public void setStoredAmount(List<Entry> storedAmount) {
		this.storedAmount = storedAmount;
	}

	public List<Entry> getDatasinks() {
		return datasinks;
	}

	public void setDatasinks(List<Entry> datasinks) {
		this.datasinks = datasinks;
	}

	public static class Entry {
		private String title;
		private int percent;

		public Entry() {
		}

		public Entry(String title, int percent) {
			this.title = title;
			this.percent = percent;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public int getPercent() {
			return percent;
		}

		public void setPercent(int percent) {
			this.percent = percent;
		}
	}
}
