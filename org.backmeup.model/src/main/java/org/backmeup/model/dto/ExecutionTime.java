package org.backmeup.model.dto;

import java.util.Date;

public class ExecutionTime {
  private Date start;
  private long delay;
  private boolean reschedule;
  
  public ExecutionTime(Date start, long delay, boolean reschedule) {
    this.start = start;
    this.delay = delay;
    this.setReschedule(reschedule);
  }
  
  public Date getStart() {
    return start;
  }
  public void setStart(Date start) {
    this.start = start;
  }
  public long getDelay() {
    return delay;
  }
  public void setDelay(long delay) {
    this.delay = delay;
  }

  public boolean isReschedule() {
    return reschedule;
  }

  public void setReschedule(boolean reschedule) {
    this.reschedule = reschedule;
  }  
}
