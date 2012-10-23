package org.backmeup.dal;

import java.util.Date;
import java.util.List;

import org.backmeup.model.JobProtocol;

public interface JobProtocolDao extends BaseDao<JobProtocol> {

  List<JobProtocol> findByUsernameAndDuration(String username, Date from, Date to);
  void deleteByUsername(String username);
}
