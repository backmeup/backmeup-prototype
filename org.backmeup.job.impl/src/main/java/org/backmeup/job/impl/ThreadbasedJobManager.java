package org.backmeup.job.impl;

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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.StatusDao;
import org.backmeup.job.JobManager;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.Status;
import org.backmeup.model.User;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageReader;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageWriter;
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
@ApplicationScoped
public class ThreadbasedJobManager implements JobManager {

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
  private BackupJobDao backupJobDao;
  private boolean started;

  public ThreadbasedJobManager() {
    this.jobs = Collections.synchronizedList(new ArrayList<BackupJob>());
    this.allJobs = Collections.synchronizedMap(new HashMap<Long, BackupJob>());
    temporaryDirectory = "temp";
  }

  public BackupJob createBackupJob(User user,
      Set<ProfileOptions> sourceProfiles, Profile sinkProfile,
      Set<ActionProfile> requiredActions, String timeExpression,
      String keyRing) {
    BackupJob bj = new BackupJob(user, sourceProfiles, sinkProfile,
        requiredActions, timeExpression);    
    bj = getBackupJobDao().save(bj);
    jobs.add(bj);
    allJobs.put(bj.getId(), bj);
    return bj;
  }
  
  private BackupJobDao getBackupJobDao() {
    if (this.backupJobDao == null) {
      this.backupJobDao = dal.createBackupJobDao();
    }
    return this.backupJobDao;
  }

  public class ThreadJobExecutor extends Thread {

    private volatile boolean running;
    
    private StatusDao statusDao;

    public ThreadJobExecutor() {
      running = true;
      setDaemon(false);
    }
    
    private StatusDao getStatusDao() {
      if (this.statusDao == null) {
        this.statusDao = dal.createStatusDao();
      }
      return this.statusDao;
    }

    public void run() {
      System.err.println("ThreadBasedExecutor: "
          + Thread.currentThread().getName());
      if (temporaryDirectory == null) {
        throw new IllegalStateException(
            "A temporary folder must be specified within bl.properties: temporaryDirectory = somefolder");
      }
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
          
          job = getBackupJobDao().findById(job.getId());
          Status s = new Status(job, String.format(
              textBundle.getString(BEGIN_JOB_MSG), job.getId()), "START",
              new Date());
          conn.begin();
          getStatusDao().save(s);
          conn.commit();

          Datasink sink = plugins.getDatasink(job.getSinkProfile().getDesc());
          Properties sinkProps = job.getSinkProfile().getEntriesAsProperties();
          boolean hasErrors = false;
          for (ProfileOptions po : job.getSourceProfiles()) {
            Datasource source = plugins
                .getDatasource(po.getProfile().getDesc());
            Properties sourceProperties = po.getProfile()
                .getEntriesAsProperties();
            try {
              StorageWriter writer = new LocalFilesystemStorageWriter();
              StorageReader reader = new LocalFilesystemStorageReader();
              try {
                DateFormat format = new SimpleDateFormat("yyyy_MM_dd hh_mm");
                String time = format.format(new Date());
                File f = new File(temporaryDirectory + "/"
                    + po.getProfile().getProfileName() + "_" + time);
                f.mkdirs();
                writer.open(f.getPath());
                reader.open(f.getPath());
              } catch (StorageException e1) {
                e1.printStackTrace();
              }
              source.downloadAll(sourceProperties, writer,
                  new ConsoleProgressor());
              s = new Status(job, String.format(textBundle
                  .getString(DOWNLOAD_COMPLETED_MSG), job.getId(), po
                  .getProfile().getProfileName()), "WORKING", new Date());
              conn.begin();
              getStatusDao().save(s);
              conn.commit();
              sink.upload(sinkProps, reader, new ConsoleProgressor());
              s = new Status(job, String.format(textBundle
                  .getString(UPLOAD_COMPLETED_MSG), job.getId(), job
                  .getSinkProfile().getProfileName()), "WORKING", new Date());
              conn.begin();
              getStatusDao().save(s);
              conn.commit();
              writer.close();
              reader.close();
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
                  "FINISHED", new Date());
              conn.begin();
              getStatusDao().save(s);
              conn.commit();
              Mailer.send(job.getUser().getEmail(), s.getMessage(), textBundle.getString(MAIL_FINISH_MSG));
            } else {
              s = new Status(job,
                  String.format(
                      textBundle.getString(FINISH_JOB_WITH_ERRORS_MSG),
                      job.getId()), "ERROR", new Date());
              conn.begin();
              getStatusDao().save(s);
              conn.commit();
              Mailer.send(job.getUser().getEmail(), s.getMessage(), textBundle.getString(MAIL_WITH_ERRORS_MSG));
            }            
          }
        }
      }
    }

    private void logErrorMessage(BackupJob job, Exception ex) {
      Status s = new Status(job, String.format(
          textBundle.getString(ERROR_JOB_MSG), job.getId(), ex.getMessage()),
          "ERROR", new Date());
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

  @Override
  public void start() {
    System.out.println("Starting up ThreadbasedJobManager!");
    if (!started) {
      je = new ThreadJobExecutor();
      je.start();
      started = true;
    }
  }

  @Override
  public BackupJob getBackUpJob(Long jobId) {
    return allJobs.get(jobId);
  }
}
