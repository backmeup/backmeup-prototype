package org.backmeup.dal.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.backmeup.dal.StatusDao;
import org.backmeup.model.Status;

public class StatusDaoImpl extends BaseDaoImpl<Status> implements StatusDao {

  public StatusDaoImpl(EntityManager em) {
    super(em);
  }

  @Override
  public List<Status> findByJob(String username, Long jobId, Date from, Date to) {
    String query = "SELECT s FROM " + entityClass.getName()
        + " s WHERE s.job.id=:jobId AND s.job.user.username=:username";
    if (from != null) {
      query = query + " AND s.timeStamp >= :from";
    }
    if (to != null) {
      query = query + " AND s.timeStamp <= :to";
    }
    TypedQuery<Status> q = em.createQuery(query, Status.class);
    q.setParameter("username", username);
    q.setParameter("jobId", jobId);    
    if (from != null)
      q.setParameter("from", from);
    if (to != null)
      q.setParameter("to", to);
    return q.getResultList();
  }

}
