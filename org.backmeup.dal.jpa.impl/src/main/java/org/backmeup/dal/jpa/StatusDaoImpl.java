package org.backmeup.dal.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.backmeup.dal.StatusDao;
import org.backmeup.model.Status;

public class StatusDaoImpl extends BaseDaoImpl<Status> implements StatusDao {

  public StatusDaoImpl(EntityManager em) {
    super(em);
  }
  
  public List<Status> findLastByJob(String username, Long jobId) {
    String query = "SELECT s FROM " + entityClass.getName()
        + " s WHERE s.job.id=:jobId AND s.job.user.username=:username ORDER BY s.timeStamp DESC";
    
    TypedQuery<Status> q = em.createQuery(query, Status.class);
    q.setParameter("username", username);
    q.setParameter("jobId", jobId);
    //q.setMaxResults(1);
    return q.getResultList();
    /*if (status.size() > 0)
      return status.get(0);
    return null;*/
  }
  
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

  @Override
  public List<Status> findByJobId(Long jobId) {
    String query = "SELECT s FROM " + entityClass.getName()
        + " s WHERE s.job.id=:jobId";
   
    TypedQuery<Status> q = em.createQuery(query, Status.class);    
    q.setParameter("jobId", jobId);        
    return q.getResultList();
  }

  @Override
  public void deleteBefore(Long jobId, Date timeStamp) {
    // Following line generates invalid postgres sql code:
    // https://hibernate.onjira.com/browse/HHH-7314
    // Query q = em.createQuery("DELETE FROM " + entityClass.getName() +" s WHERE s.job.id = :jobId AND s.timeStamp <= :timeStamp");
    // q.setParameter("username", username);
    // q.setParameter("timeStamp", timeStamp);
    // q.executeUpdate();
    // workaround:
    TypedQuery<Status> status = em.createQuery("SELECT s FROM " + entityClass.getName() + " s WHERE s.job.id = :jobId AND s.timeStamp <= :timeStamp", Status.class);
    status.setParameter("jobId", jobId);
    status.setParameter("timeStamp", timeStamp, TemporalType.TIMESTAMP);
    for(Status s : status.getResultList()) {
      em.remove(s);
    }    
  }

}
