package org.backmeup.dal;

import java.util.Date;
import java.util.List;

import org.backmeup.model.Status;

public interface StatusDao extends BaseDao<Status> {
  public List<Status> findByJob(String username, Long jobId, Date from, Date to);
  public List<Status> findByJobId(Long jobId);
}
