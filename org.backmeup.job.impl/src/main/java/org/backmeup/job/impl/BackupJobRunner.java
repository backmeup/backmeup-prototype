package org.backmeup.job.impl;

import java.util.Date;
import java.util.Properties;

import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.StatusDao;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.Status;
import org.backmeup.model.Token;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;

/**
 * Implements the actual BackupJob execution.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at.>
 */
public class BackupJobRunner {

  private Plugin plugins;
  private Keyserver keyserver;
  private Connection conn;
  private DataAccessLayer dal;

  public BackupJobRunner(Plugin plugins, Keyserver keyserver, Connection conn,
      DataAccessLayer dal) {
    this.plugins = plugins;
    this.keyserver = keyserver;
    this.conn = conn;
    this.dal = dal;
  }

  private void addStatusToDb(Status status) {
    conn.beginOrJoin();    
    StatusDao sd = dal.createStatusDao();
    sd.save(status); // store job within database
    conn.commit(); // commit to the database and to the core
  }

  public void executeBackup(BackupJob job, StorageReader storageReader,
      StorageWriter storageWriter) {
    String tempDir = "job-" + System.currentTimeMillis();
    try {
      conn.beginOrJoin();
      BackupJobDao bjd = dal.createBackupJobDao();
      job = bjd.findById(job.getId()); // use the job which is stored within the database      
      // when will the next access to the access data occur? current time +
      // delay
      job.getToken().setBackupdate(new Date().getTime() + job.getDelay());
      // get access data + new token for next access
      AuthDataResult authenticationData = keyserver.getData(job.getToken());
      // the token for the next getData call
      Token newToken = authenticationData.getNewToken();
      job.setToken(newToken);
      // TODO: store newToken for the next backup schedule
      conn.commit(); // stores the new token within database
      
      Datasink sink = plugins
          .getDatasink(job.getSinkProfile().getDescription());
      Properties sinkProperties = authenticationData.getByProfileId(job
          .getSinkProfile().getProfileId());
      addStatusToDb(new Status(job, "BackupJob started", "info", new Date()));
      // TODO insert updates to the BackupJobStatus DB entity
      for (ProfileOptions po : job.getSourceProfiles()) {
        // Download from Source
        System.out.println("Downloading to temporary storage");
        Datasource source = plugins.getDatasource(po.getProfile()
            .getDescription());
        Properties sourceProperties = authenticationData.getByProfileId(po
            .getProfile().getProfileId());
        try {
          storageWriter.open(tempDir);
          source.downloadAll(sourceProperties, storageWriter,
              new Progressable() {
                @Override
                public void progress(String message) {
                  System.out.println(message);
                }
              });
          storageWriter.close();
        } catch (StorageException e) {
          // TODO error handling
          System.out.println("ERROR: " + e.getMessage());
        } catch (DatasourceException e) {
          // TODO error handling
          System.out.println("ERROR: " + e.getMessage());
        }
        System.out.println("Download complete.");
        addStatusToDb(new Status(job, "Download completed", "info", new Date()));
        // Execute Actions in sequence
        try {
          conn.beginOrJoin();
          job = dal.createBackupJobDao().findById(job.getId());
          for (ActionProfile actionProfile : job.getRequiredActions()) {
            // TODO get actions from plugins & execute
          }
        }
        finally {
         conn.rollback(); 
        }

        // Upload to Sink
        System.out.println("Uploading to Datasink");

        try {
          storageReader.open(tempDir);

          sink.upload(sinkProperties, storageReader, new Progressable() {
            @Override
            public void progress(String message) {
              System.out.println(message);
            }
          });

          storageReader.close();
        } catch (StorageException e) {
          // TODO error handling
          System.out.println("ERROR: " + e.getMessage());
        }
        System.out.println("Upload complete.");
        addStatusToDb(new Status(job, "BackupJob completed", "info", new Date()));
      }
    } finally {
      conn.rollback();
    }
  }

}
