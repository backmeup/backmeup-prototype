package org.backmeup.job.impl.threadbased;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.StatusDao;
import org.backmeup.keyserver.client.AuthData;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.Status;
import org.backmeup.model.Token;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.UserProperty;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.backmeup.utilities.mail.Mailer;

/**
 * The ThreadbasedJobManager creates a thread which will periodically check for
 * a new backup job, executing it and printing all errors to stderr.
 * 
 * It will be replaced by a robust high scaling version.
 * 
 * @author fschoeppl
 * 
 */
// @ApplicationScoped
public class ThreadbasedJobManager /* implements JobManager  */{

  public class ConsoleProgressor implements Progressable {
    public void progress(String message) {
      System.out.println(message);
    }
  }

  private ThreadJobExecutor je;
  private List<BackupJob> jobs;
  private Map<Long, BackupJob> allJobs;

  private ResourceBundle textBundle = ResourceBundle
      .getBundle(ThreadbasedJobManager.class.getSimpleName());

  private static final String BEGIN_JOB_MSG = "org.backmeup.job.impl.ThreadbasedJobManager.BEGIN_JOB";
  private static final String DOWNLOAD_COMPLETED_MSG = "org.backmeup.job.impl.ThreadbasedJobManager.DOWNLOAD_COMPLETED";
  private static final String UPLOAD_COMPLETED_MSG = "org.backmeup.job.impl.ThreadbasedJobManager.UPLOAD_COMPLETED";
  private static final String ERROR_JOB_MSG = "org.backmeup.job.impl.ThreadbasedJobManager.ERROR_JOB";
  private static final String FINISH_JOB_MSG = "org.backmeup.job.impl.ThreadbasedJobManager.FINISHED_JOB";
  private static final String FINISH_JOB_WITH_ERRORS_MSG = "org.backmeup.job.impl.ThreadbasedJobManager.FINISHED_JOB_WITH_ERRORS";
  private static final String MAIL_FINISH_MSG = "org.backmeup.job.impl.ThreadbasedJobManager.MAIL_FINISH_MSG";
  private static final String MAIL_WITH_ERRORS_MSG = "org.backmeup.job.impl.ThreadbasedJobManager.MAIL_WITH_ERRORS_MSG";
  @Inject
  private Plugin plugins;

  @Inject
  private DataAccessLayer dal;

  @Inject
  private Connection conn;

  @Inject
  @Named("job.temporaryDirectory")
  private String temporaryDirectory;
  
  @Inject
  @Named("job.backupname")
  private String backupNamePattern;
  
  @Inject
  private Keyserver keyserver;
  
  private boolean started;

  public ThreadbasedJobManager() {
    this.jobs = Collections.synchronizedList(new ArrayList<BackupJob>());
    this.allJobs = Collections.synchronizedMap(new HashMap<Long, BackupJob>());
    //temporaryDirectory = "temp"; 
  } 

  public BackupJob createBackupJob(BackMeUpUser user,
      Set<ProfileOptions> sourceProfiles, Profile sinkProfile,
      List<ActionProfile> requiredActions, Date start, long delay, String keyRing, String jobTitle, boolean reschedule, String encryptionPwd) {
    
    BackupJob bj = new BackupJob(user, sourceProfiles, sinkProfile,
        requiredActions, start, delay, jobTitle, reschedule);
   
    Long executionTime = start.getTime() + delay;
    Token t = keyserver.getToken(bj, keyRing, executionTime, true, encryptionPwd);    
    bj.setToken(t);
    bj = getBackupJobDao().save(bj);
    jobs.add(bj);
    allJobs.put(bj.getId(), bj);
    return bj;
  }

  private BackupJobDao getBackupJobDao() {
    return dal.createBackupJobDao();
  }

  public class ThreadJobExecutor extends Thread {

    private volatile boolean running;

    public ThreadJobExecutor() {
      running = true;
      setDaemon(false);
    }

    private StatusDao getStatusDao() {
      return dal.createStatusDao();
    }

    public void run() {      
      System.err.println("ThreadBasedExecutor: "
          + Thread.currentThread().getName());
      if (temporaryDirectory == null) {
        throw new IllegalStateException(
            "The temporary folder must be specified within bl.properties, e.g. job.temporaryDirectory = somefolder");
      }
      
      if (backupNamePattern == null) {
        throw new IllegalStateException(
            "The backup name must be specified within bl.properties, e.g.: job.backupname = BMU_%SOURCE%_dd_MM_YYYY_hh_mm");
      }
      DateFormat df = new SimpleDateFormat(backupNamePattern.trim());
      temporaryDirectory = temporaryDirectory + "/cache";
      while (isRunning()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (!jobs.isEmpty()) {
          BackupJob job = jobs.get(0);
          jobs.remove(0);
          try {
            conn.begin();
            job.getUser().getUserProperty(UserProperty.PROP_KEEP_BACKUP);
            //TODO: Keep "keepCnt" backups and start to overwrite the very first one if you hit "keepCnt".
            job = getBackupJobDao().findById(job.getId());
            Status s = new Status(job, String.format(
                textBundle.getString(BEGIN_JOB_MSG), job.getId()), "START", "backupjob",
                new Date());

            getStatusDao().save(s);
            conn.commit();
            
            AuthDataResult adr = keyserver.getData(job.getToken());            
            Map<Long, AuthData> authData = new HashMap<Long, AuthData>();
            for (AuthData ad : adr.getAuthinfos()) {
              authData.put(ad.getBmu_authinfo_id(), ad);
            }

            Datasink sink = plugins.getDatasink(job.getSinkProfile().getDescription());
            Properties sinkProps = authData.get(job.getSinkProfile().getProfileId()).getAiData();
            boolean hasErrors = false;
            for (ProfileOptions po : job.getSourceProfiles()) {
              Datasource source = plugins.getDatasource(po.getProfile()
                  .getDescription());
              Properties sourceProperties = authData.get(po.getProfile().getProfileId()).getAiData();
              try {
                Storage storage = new LocalFilesystemStorage();
                try {
                  String folderName = df.format(new Date()).replace("%SOURCE%", po.getProfile().getProfileName());
                  File f = new File(temporaryDirectory + "/" + folderName);
                  f.mkdirs();
                  storage.open(f.getPath());
                } catch (StorageException e1) {
                  e1.printStackTrace();
                }
                source.downloadAll(sourceProperties, new ArrayList<String>(), storage,
                    new ConsoleProgressor());
                s = new Status(job, String.format(
                    textBundle.getString(DOWNLOAD_COMPLETED_MSG), job.getId(),
                    po.getProfile().getProfileName()), "WORKING", "backupjob", new Date());
                conn.begin();
                getStatusDao().save(s);
                conn.commit();
                sink.upload(sinkProps, storage, new ConsoleProgressor());
                s = new Status(job, String.format(
                    textBundle.getString(UPLOAD_COMPLETED_MSG), job.getId(),
                    job.getSinkProfile().getProfileName()), "WORKING", "backupjob",
                    new Date());
                conn.begin();
                getStatusDao().save(s);
                conn.commit();
                storage.close();
              } catch (DatasourceException e) {
                e.printStackTrace();
                logErrorMessage(job, e);
                hasErrors = true;
              } catch (StorageException e) {
                e.printStackTrace();
                logErrorMessage(job, e);
                hasErrors = true;
              } catch (BackMeUpException me) {
                me.printStackTrace();
                logErrorMessage(job, me);
                hasErrors = true;
              } catch (Exception ex) {
                ex.printStackTrace();
                logErrorMessage(job, ex);
                hasErrors = true;
              }
              if (!hasErrors) {
                s = new Status(job, String.format(
                    textBundle.getString(FINISH_JOB_MSG), job.getId()),
                    "FINISHED", "backupjob", new Date());
                conn.begin();
                getStatusDao().save(s);
                conn.commit();
                Mailer.send(job.getUser().getEmail(), s.getMessage(),
                    textBundle.getString(MAIL_FINISH_MSG));
              } else {
                s = new Status(job, String.format(
                    textBundle.getString(FINISH_JOB_WITH_ERRORS_MSG),
                    job.getId()), "ERROR", "backupjob", new Date());
                conn.begin();
                getStatusDao().save(s);
                conn.commit();
                Mailer.send(job.getUser().getEmail(), s.getMessage(),
                    textBundle.getString(MAIL_WITH_ERRORS_MSG));
              }
            }
          } catch (Exception ex) {
            // TODO: Log exception
            ex.printStackTrace();
          } finally {
            conn.rollback();
          }
        }
      }
    }

    private void logErrorMessage(BackupJob job, Exception ex) {
      Status s = new Status(job, String.format(
          textBundle.getString(ERROR_JOB_MSG), job.getId(), ex.getMessage()),
          "ERROR", "backupjob", new Date());
      conn.begin();
      getStatusDao().save(s);
      conn.commit();
    }

    public boolean isRunning() {
      return running;
    }

    public void setRunning(boolean running) {
      this.running = running;
    }
  }

  public void shutdown() {
    if (started) {
      System.out.println("Shutting down ThreadbasedJobManager!");
      this.je.setRunning(false);
      try {
        je.join();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
      started = false;
    }
  }

  public Plugin getPlugins() {
    return plugins;
  }

  public void setPlugins(Plugin plugins) {
    this.plugins = plugins;
  }

  // @Override
  public void start() {
    System.out.println("Starting up ThreadbasedJobManager!");
    if (!started) {
      je = new ThreadJobExecutor();
      je.start();
      started = true;
    }
  }

  // @Override
  public BackupJob getBackUpJob(Long jobId) {
    return allJobs.get(jobId);
  }
}
