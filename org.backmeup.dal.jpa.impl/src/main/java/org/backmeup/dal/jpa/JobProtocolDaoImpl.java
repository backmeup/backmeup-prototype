package org.backmeup.dal.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.backmeup.dal.JobProtocolDao;
import org.backmeup.model.JobProtocol;

public class JobProtocolDaoImpl extends BaseDaoImpl<JobProtocol> implements
    JobProtocolDao {

  public JobProtocolDaoImpl(EntityManager em) {
    super(em);
  }

  @Override
  public List<JobProtocol> findByUsernameAndDuration(String username, Date from, Date to) {
    TypedQuery<JobProtocol> q = em.createQuery("SELECT p FROM " + entityClass.getName() +" p WHERE p.user.username = :username AND p.executionTime >= :from AND p.executionTime <= :to", JobProtocol.class);
    q.setParameter("username", username);
    q.setParameter("from", from);
    q.setParameter("to", to);
    return q.getResultList();
  }
}
