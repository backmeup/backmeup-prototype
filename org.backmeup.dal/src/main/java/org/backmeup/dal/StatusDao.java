package org.backmeup.dal;

import java.util.Date;
import java.util.List;

import org.backmeup.model.Status;

public interface StatusDao extends BaseDao<Status> {
  public List<Status> findLastByJob(String username, Long jobId);
  public List<Status> findByJobId(Long jobId);
  public void deleteBefore(Long jobId, Date timeStamp);
}
