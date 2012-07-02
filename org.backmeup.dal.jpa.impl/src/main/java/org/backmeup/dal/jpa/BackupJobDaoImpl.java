package org.backmeup.dal.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.model.BackupJob;

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

}
