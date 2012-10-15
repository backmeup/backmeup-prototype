package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.model.BackupJob;
import org.backmeup.model.JobProtocol;

public class BackupJobDaoImpl extends BaseDaoImpl<BackupJob> implements
    BackupJobDao {

  public BackupJobDaoImpl(EntityManager em) {
    super(em);
  }

  @Override
  public List<BackupJob> findByUsername(String username) {
    //TODO: Change all queries to named parameter (instead of numbered)
    TypedQuery<BackupJob> q = em.createQuery("SELECT j FROM " + entityClass.getName() +" j WHERE j.user.username = :username", entityClass);
    q.setParameter("username", username);
    List<BackupJob> jobs = q.getResultList();   
    return jobs;
  }

  @Override
  public List<BackupJob> findAll() {
    TypedQuery<BackupJob> q = em.createQuery("SELECT j FROM " + entityClass.getName() +" j", entityClass);    
    List<BackupJob> jobs = q.getResultList();   
    return jobs;
  }

  @Override
  public BackupJob findLastBackupJob(String username) {
    TypedQuery<BackupJob> q = em.createQuery("SELECT jp.job FROM " + JobProtocol.class.getName() +" jp WHERE jp.user.username = :username ORDER BY jp.executionTime DESC", entityClass);
    //TypedQuery<BackupJob> q = em.createQuery("SELECT j FROM " + entityClass.getName() +" j WHERE j.user.username = :username ORDER BY j.created DESC", entityClass);
    q.setParameter("username", username);
    q.setMaxResults(1);
    List<BackupJob> jobs = q.getResultList();
    if (jobs.size() > 0)
      return jobs.get(0);
    return null;
  } 

}
