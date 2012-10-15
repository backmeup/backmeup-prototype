package org.backmeup.dal;

import java.util.List;

import org.backmeup.model.BackupJob;


public interface BackupJobDao extends BaseDao<BackupJob> { 
  public List<BackupJob> findByUsername(String username);
  public BackupJob findLastBackupJob(String username);
  public List<BackupJob> findAll();
}
