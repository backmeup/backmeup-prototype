package org.backmeup.dal.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.JobProtocolDao;
import org.backmeup.dal.ProfileDao;
import org.backmeup.dal.SearchResponseDao;
import org.backmeup.dal.ServiceDao;
import org.backmeup.dal.StatusDao;
import org.backmeup.dal.UserDao;

/**
 * The DataAccessLayerImpl uses JPA to interact with the underlying database.
 * 
 * @author fschoeppl
 * 
 */
@ApplicationScoped
public class DataAccessLayerImpl implements DataAccessLayer {
  private ThreadLocal<EntityManager> threaLocalEntityManager = new ThreadLocal<EntityManager>();

  public DataAccessLayerImpl() {
  }

  public UserDao createUserDao() {
    return new UserDaoImpl(threaLocalEntityManager.get());
  }

  public ProfileDao createProfileDao() {
    return new ProfileDaoImpl(threaLocalEntityManager.get());
  }

  public StatusDao createStatusDao() {
    return new StatusDaoImpl(threaLocalEntityManager.get());
  }
  
  public SearchResponseDao createSearchResponseDao() {
    return new SearchResponseDaoImpl(threaLocalEntityManager.get());
  }

  public BackupJobDao createBackupJobDao() {
    return new BackupJobDaoImpl(threaLocalEntityManager.get());
  }

  public ServiceDao createServiceDao() {    
    return new ServiceDaoImpl(threaLocalEntityManager.get());
  }
  
  @Override
  public JobProtocolDao createJobProtocolDao() {
    return new JobProtocolDaoImpl(threaLocalEntityManager.get());
  }

  public void setConnection(Object connection) {
    this.threaLocalEntityManager.set((EntityManager) connection);
  }
}
