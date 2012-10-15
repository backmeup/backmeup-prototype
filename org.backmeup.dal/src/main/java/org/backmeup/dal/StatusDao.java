package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.Status;

public interface StatusDao extends BaseDao<Status> {
  public Status findLastByJob(String username, Long jobId);
  public List<Status> findByJobId(Long jobId);
}
