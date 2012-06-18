package org.backmeup.dal.jpa;

import javax.persistence.EntityManager;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.model.BackupJob;

public class BackupJobDaoImpl extends BaseDaoImpl<BackupJob> implements
    BackupJobDao {

  public BackupJobDaoImpl(EntityManager em) {
    super(em);
  } 

}
