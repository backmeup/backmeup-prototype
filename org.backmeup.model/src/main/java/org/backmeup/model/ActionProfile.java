package org.backmeup.model;

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

@Entity
public class ActionProfile implements Comparable<ActionProfile> {
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;
  private String actionId;
  private int priority;
  @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true, mappedBy="profile")
  private Set<ActionProperty> actionOptions = new HashSet<ActionProperty>();
  
  public ActionProfile() {
  }
  
  public ActionProfile(String actionId, int priority) {
    this.actionId = actionId;
    this.setPriority(priority);    
  }
  
  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getActionId() {
    return actionId;
  }
  
  public void setActionId(String actionId) {
    this.actionId = actionId;
  }

  @Override
  public int compareTo(ActionProfile o) {
    if (o == null)
      return -1;    
    return o.getPriority() - this.getPriority();
  }
  
  public Set<ActionProperty> getActionOptions() {
    return actionOptions;
  }

  public void addActionOption(String key, String value) {
    ActionProperty ap = new ActionProperty(key, value);
    ap.setProfile(this);
    this.actionOptions.add(ap);
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  @Entity
  @Table(name="ActionProfileProperty")
  public static class ActionProperty {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    private String key;
    private String value;
    @ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.EAGER)
    private ActionProfile profile;
           
    public ActionProperty() {
    }

    public ActionProperty(String key, String value) {
      this.key = key;
      this.value = value;
    }
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public Long getId() {
      return id;
    }

    public ActionProfile getProfile() {
      return profile;
    }

    public void setProfile(ActionProfile profile) {
      this.profile = profile;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
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
      ActionProperty other = (ActionProperty) obj;
      if (key == null) {
        if (other.key != null)
          return false;
      } else if (!key.equals(other.key))
        return false;
      return true;
    }
  }
}
