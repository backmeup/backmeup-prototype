package org.backmeup.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class contains a protocol overview
 * about a certain user.
 * 
 * @author fschoeppl
 *
 */
public class ProtocolOverview {
  private Long userId;  
	private String totalCount;
	private String totalStored;
	private Set<Entry> storedAmount = new HashSet<Entry>();	
	private List<Activity> activities = new ArrayList<Activity>();
	
	public ProtocolOverview() {
	}

	public ProtocolOverview(String totalCount, String totalStored,
			Set<Entry> storedAmount) {
		this.totalCount = totalCount;
		this.totalStored = totalStored;
		this.storedAmount = storedAmount;
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

	public Set<Entry> getStoredAmount() {
		return storedAmount;
	}

	public void setStoredAmount(Set<Entry> storedAmount) {
		this.storedAmount = storedAmount;
	}
	
	public Long getUser() {
    return userId;
  }

  public void setUser(Long userId) {
    this.userId = userId;
  }
  
  public List<Activity> getActivities() {
    return activities;
  }

  public void setActivities(List<Activity> activities) {
    this.activities = activities;
  }

  public static class Activity {
    private String title;
    private Date backupDate;
    public Activity(String title, Date backupDate) {
      this.title = title;
      this.backupDate = backupDate;
    }
    public Activity() {
    }
    public String getTitle() {
      return title;
    }
    public void setTitle(String title) {
      this.title = title;
    }
    public Date getBackupDate() {
      return backupDate;
    }
    public void setBackupDate(Date backupDate) {
      this.backupDate = backupDate;
    }
  }

  public static class Entry {
		private String title;
		private double percent;
		private double absolute;

		public Entry() {
		}

		public Entry(String title, int percent, double absolute) {
			this.title = title;
			this.percent = percent;
			this.setAbsolute(absolute);
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public double getPercent() {
			return percent;
		}

		public void setPercent(double percent) {
			this.percent = percent;
		}

    public double getAbsolute() {
      return absolute;
    }

    public void setAbsolute(double absolute) {
      this.absolute = absolute;
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((title == null) ? 0 : title.hashCode());
      return result;
    }  

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Entry other = (Entry) obj;
      if (title == null) {
        if (other.title != null)
          return false;
      } else if (!title.equals(other.title))
        return false;
      return true;
    }
	}
}
