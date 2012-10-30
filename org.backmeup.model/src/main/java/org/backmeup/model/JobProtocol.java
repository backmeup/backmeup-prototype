package org.backmeup.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class JobProtocol {
  @Id 
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;
  @Temporal(TemporalType.TIMESTAMP)
  private Date executionTime;
  @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
  private BackMeUpUser user;
  private boolean successful;
  private long totalStoredEntries;  
  private String sinkTitle;
  @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
  private BackupJob job;
  @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
  private Set<JobProtocolMember> members = new HashSet<JobProtocolMember>();
  
  public JobProtocol() {
  }

  public JobProtocol(Date executionTime, BackMeUpUser user, boolean successful,
      long totalStoredEntries, String sinkTitle, Set<JobProtocolMember> members) {
    this.executionTime = executionTime;
    this.user = user;
    this.successful = successful;
    this.totalStoredEntries = totalStoredEntries;
    this.sinkTitle = sinkTitle;
    this.members.addAll(members);
  }
  
  public Date getExecutionTime() {
    return executionTime;
  }

  public void setExecutionTime(Date executionTime) {
    this.executionTime = executionTime;
  }

  public BackMeUpUser getUser() {
    return user;
  }

  public void setUser(BackMeUpUser user) {
    this.user = user;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public void setSuccessful(boolean successful) {
    this.successful = successful;
  }

  public long getTotalStoredEntries() {
    return totalStoredEntries;
  }

  public void setTotalStoredEntries(long totalStoredEntries) {
    this.totalStoredEntries = totalStoredEntries;
  }

  public String getSinkTitle() {
    return sinkTitle;
  }

  public void setSinkTitle(String sinkTitle) {
    this.sinkTitle = sinkTitle;
  }

  public Set<JobProtocolMember> getMembers() {
    return members;
  }

  public void addMembers(Set<JobProtocolMember> members) {
    this.members.addAll(members);
  }
  
  public void addMember(JobProtocolMember member) {
    this.members.add(member);
  }

  public Long getId() {
    return id;
  }

  public BackupJob getJob() {
    return job;
  }

  public void setJob(BackupJob job) {
    this.job = job;
  }

  @Entity
  @Table(name="JobProtocolMember")
  public static class JobProtocolMember {
    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
    private JobProtocol protocol;
    private String title;
    private double space;
    
    public JobProtocolMember() {
    }

    public JobProtocolMember(JobProtocol protocol, String title,
        double space) {      
      this.protocol = protocol;
      this.title = title;
      this.space = space;
    }

    public JobProtocol getProtocol() {
      return protocol;
    }

    public void setProtocol(JobProtocol protocol) {
      this.protocol = protocol;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public double getSpace() {
      return space;
    }

    public void setSpace(double space) {
      this.space = space;
    }

    public Long getId() {
      return id;
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
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
      JobProtocolMember other = (JobProtocolMember) obj;
      if (id == null) {
        if (other.id != null)
          return false;
      } else if (!id.equals(other.id))
        return false;
      return true;
    }
  }
}
