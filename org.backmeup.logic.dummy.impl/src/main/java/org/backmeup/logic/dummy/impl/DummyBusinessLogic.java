package org.backmeup.logic.dummy.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.backmeup.logic.BusinessLogic;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.AuthRequest;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.FileItem;
import org.backmeup.model.KeyserverLog;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.ProtocolDetails;
import org.backmeup.model.ProtocolDetails.FileInfo;
import org.backmeup.model.ProtocolDetails.Sink;
import org.backmeup.model.ProtocolOverview;
import org.backmeup.model.ProtocolOverview.Entry;
import org.backmeup.model.SearchResponse;
import org.backmeup.model.SearchResponse.CountedEntry;
import org.backmeup.model.SearchResponse.SearchEntry;
import org.backmeup.model.Status;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.dto.ActionProfileEntry;
import org.backmeup.model.dto.JobCreationRequest;
import org.backmeup.model.dto.SourceProfileEntry;
import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.model.exceptions.InvalidCredentialsException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.model.spi.SourceSinkDescribable.Type;
import org.backmeup.model.spi.ValidationExceptionType;

/**
 * The dummy businness logic stores all data within Lists and Maps (in-memory).
 * It realizes all operations and can be used as mock up to test the rest layer
 * or create a rest client.
 * 
 * @author fschoeppl
 * 
 */
@ApplicationScoped
public class DummyBusinessLogic implements BusinessLogic {
  Long maxId = 3l;

  private List<BackMeUpUser> knownUsers;
  private List<SourceSinkDescribable> sources;
  private List<SourceSinkDescribable> sinks;
  private List<Profile> profiles;
  private List<BackupJob> jobs;
  private List<Status> status;
  private Map<String, ActionDescribable> actions;
  private Map<Long, SearchResponse> searches;
  private Map<BackMeUpUser, String> passwords;

  private static final long DELAY_DAILY = 24 * 60 * 60 * 1000;
  private static final long DELAY_WEEKLY = 24 * 60 * 60 * 1000 * 7;
  private static final long DELAY_MONTHLY = (long) (24 * 60 * 60 * 1000
      * 365.242199 / 12.0);
  private static final long DELAY_YEARLY = (long) (24 * 60 * 60 * 1000 * 365.242199);

  public DummyBusinessLogic() {
    BackMeUpUser u1 = new BackMeUpUser(0l, "Sepp", "sepp@mail.at");
    BackMeUpUser u2 = new BackMeUpUser(1l, "Marion", "marion@mail.at");
    BackMeUpUser u3 = new BackMeUpUser(2l, "Phil", "em");
    knownUsers = new ArrayList<BackMeUpUser>();
    knownUsers.add(u1);
    knownUsers.add(u2);
    knownUsers.add(u3);
    passwords = new HashMap<BackMeUpUser, String>();
    passwords.put(u1, "pw");
    passwords.put(u2, "1234");
    passwords.put(u3, "p1");

    sources = new ArrayList<SourceSinkDescribable>();
    sources.add(new SourceSinkDescribable() {

      public String getImageURL() {
        return "http://skydrive.image.png";
      }

      public String getId() {
        return "org.backmeup.skydrive";
      }

      public String getTitle() {
        return "SkyDrive";
      }

      public String getDescription() {
        return "Information";
      }

      public Type getType() {
        return Type.Source;
      }

      @Override
      public Properties getMetadata(Properties accessData) {
        Properties props = new Properties();
        props.setProperty(org.backmeup.plugin.api.Metadata.BACKUP_FREQUENCY,
            "daily");
        return props;
      }
    });

    sources.add(new SourceSinkDescribable() {

      public String getImageURL() {
        return "http://dropbox.image.png";
      }

      public String getId() {
        return "org.backmeup.dropbox";
      }

      public String getTitle() {
        return "Dropbox";
      }

      public String getDescription() {
        return "Information";
      }

      public Type getType() {
        return Type.Source;
      }

      @Override
      public Properties getMetadata(Properties accessData) {
        Properties props = new Properties();
        props.setProperty(org.backmeup.plugin.api.Metadata.BACKUP_FREQUENCY,
            "weekly");
        return props;
      }
    });

    sinks = new ArrayList<SourceSinkDescribable>();
    sinks.add(new SourceSinkDescribable() {

      public String getImageURL() {
        return "http://wuala.image.png";
      }

      public String getId() {
        return "org.backmeup.wuala";
      }

      public String getTitle() {
        return "Wuala";
      }

      public String getDescription() {
        return "Information";
      }

      public Type getType() {
        return Type.Sink;
      }

      @Override
      public Properties getMetadata(Properties accessData) {
        Properties props = new Properties();
        props.setProperty(org.backmeup.plugin.api.Metadata.BACKUP_FREQUENCY,
            "daily");
        props.setProperty(org.backmeup.plugin.api.Metadata.FILE_SIZE_LIMIT,
            "100");
        props.setProperty(org.backmeup.plugin.api.Metadata.QUOTA, "500");
        props.setProperty(org.backmeup.plugin.api.Metadata.QUOTA_LIMIT, "2000");
        return props;
      }
    });

    sinks.add(new SourceSinkDescribable() {
      public String getImageURL() {
        return "http://dvd.image.png";
      }

      public String getId() {
        return "org.backmeup.dvd";
      }

      public String getTitle() {
        return "DVD";
      }

      public String getDescription() {
        return "Information";
      }

      public Type getType() {
        return Type.Sink;
      }

      @Override
      public Properties getMetadata(Properties accessData) {
        Properties props = new Properties();
        props.setProperty(org.backmeup.plugin.api.Metadata.BACKUP_FREQUENCY,
            "daily");
        props.setProperty(org.backmeup.plugin.api.Metadata.FILE_SIZE_LIMIT,
            "700");
        props.setProperty(org.backmeup.plugin.api.Metadata.QUOTA, "500");
        props.setProperty(org.backmeup.plugin.api.Metadata.QUOTA_LIMIT, "700");
        return props;
      }
    });

    profiles = new ArrayList<Profile>();
    profiles.add(new Profile(500l, u1, "Dropbox-Source",
        "org.backmeup.dropbox", Type.Source));
    profiles.add(new Profile(501l, u1, "Wuala-Sink", "org.backmeup.wuala",
        Type.Sink));

    profiles.add(new Profile(502l, u3, "Dropbox-Source",
        "org.backmeup.dropbox", Type.Source));
    profiles.add(new Profile(503l, u3, "Wuala-Sink", "org.backmeup.wuala",
        Type.Sink));
    jobs = new ArrayList<BackupJob>();

    actions = new HashMap<String, ActionDescribable>();
    actions.put("org.backmeup.rsa", new ActionDescribable() {
      public String getTitle() {
        return "Verschluesselung";
      }

      public String getId() {
        return "org.backmeup.rsa";
      }

      public String getDescription() {
        return "Verschluesselt Ihre Daten mit RSA";
      }

      @Override
      public Properties getMetadata(Properties accessData) {
        Properties props = new Properties();
        return props;
      }

      @Override
      public int getPriority() {
        return 200;
      }
      
      @Override
      public List<String> getAvailableOptions()
      {
    	  return new LinkedList<String> ();
      }
    });

    List<ActionProfile> reqActions = new ArrayList<ActionProfile>();
    reqActions.add(new ActionProfile("org.backmeup.rsa", 1));
    Set<ProfileOptions> popts = new HashSet<ProfileOptions>();
    popts.add(new ProfileOptions(findProfile(500), null));
    BackupJob aJob = new BackupJob(u1, popts, findProfile(501), reqActions,
        new Date(), 5000, "TestJob");
    aJob.setId(maxId++);
    jobs.add(aJob);
    status = new ArrayList<Status>();
    status.add(new Status(aJob, "Der Backup-Job wurde gestartet", "INFO", "backupjob",
        new Date(100)));
    status.add(new Status(aJob, "Der Backup-Job wurde unterbrochen", "WARN", "backupjob",
        new Date(500)));
    status.add(new Status(aJob, "Der Backup-Job wurde erfolgreich beendet", "backupjob",
        "INFO", new Date(1000)));

    Set<ProfileOptions> popts2 = new HashSet<ProfileOptions>();
    popts2.add(new ProfileOptions(findProfile(502), null));
    BackupJob bJob = new BackupJob(u3, popts2, findProfile(502), reqActions,
        new Date(), 5000, "TestJob");
    bJob.setId(maxId++);
    jobs.add(bJob);
    status.add(new Status(bJob, "Ein Status", "INFO", "backupjob", new Date(100)));
    status.add(new Status(bJob, "Noch ein Status", "INFO", "backupjob", new Date(100)));
    Set<FileItem> files = new HashSet<FileItem>();
    files.add(new FileItem("http://thumbnails.at?url=1234", "sennenhund.jpg",
        new Date(100)));
    status.add(new Status(bJob, "Busy status", "STORE", "backupjob", new Date(100), "BUSY",
        files));
    searches = new HashMap<Long, SearchResponse>();
  }

  // user operations

  public BackMeUpUser getUser(String username) {
    BackMeUpUser u = findUser(username);
    if (u == null)
      throw new UnknownUserException(username);
    return u;
  }

  public BackMeUpUser changeUser(String oldUsername, String newUsername, String oldPassword,
      String newPassword,  String newEmail) {
    BackMeUpUser u = findUser(oldUsername);
    if (findUser(newUsername) != null) {
      throw new AlreadyRegisteredException(newUsername);
    }
    if (newUsername != null) {
      u.setUsername(newUsername);
    }
    /*
     * if (!u.getPassword().equals(oldPassword)) throw new
     * InvalidCredentialsException(); if (newPassword != null)
     * u.setPassword(newPassword); if (newKeyRing != null)
     * u.setKeyRing(newKeyRing);
     */
    if (newEmail != null)
      u.setEmail(newEmail);
    return u;
  }

  public BackMeUpUser deleteUser(String username) {
    BackMeUpUser u = findUser(username);
    if (u == null)
      throw new UnknownUserException(username);
    knownUsers.remove(u);
    return u;
  }

  public BackMeUpUser login(String username, String password) {
    for (BackMeUpUser u : knownUsers) {
      if (u.getUsername().equals(username) && passwords.get(u).equals(password)) {
        return u;
      }
    }
    throw new InvalidCredentialsException();
  }

  public BackMeUpUser register(String username, String password, String keyRing,
      String email) {
    if (username == null || password == null || keyRing == null
        || email == null)
      throw new IllegalArgumentException("One of the given parameters is null!");
    for (BackMeUpUser u : knownUsers) {
      if (u.getUsername().equals(username))
        throw new AlreadyRegisteredException(u.getUsername());
    }
    BackMeUpUser user = new BackMeUpUser(maxId++, username, email);
    passwords.put(user, password);
    knownUsers.add(user);
    return user;
  }

  // datasource operations

  public Profile deleteProfile(String username, Long profile) {
    Profile p = findProfile(profile);
    if (p.getUser().getUsername().equals(username)) {
      profiles.remove(p);
      return p;
    }
    throw new IllegalArgumentException(String.format(
        "Unknown profile %d (user %s)", username, profile));
  }

  public List<SourceSinkDescribable> getDatasources() {
    return sources;
  }

  public List<Profile> getDatasourceProfiles(String username) {
    List<Profile> cloneDs = new ArrayList<Profile>();
    for (Profile p : profiles) {
      if (findSourceDescribable(p.getDescription()) != null
          && p.getUser().getUsername().equals(username)) {
        cloneDs.add(p);
      }
    }
    return cloneDs;
  }

  public List<String> getDatasourceOptions(String username, Long profileId,
      String keyRingPassword) {
    Profile p = findProfile(profileId);
    if (p == null)
      throw new IllegalArgumentException("Invalid profile " + profileId);

    SourceSinkDescribable ssd = findSourceSinkDescribable(p.getDescription());
    if (ssd == null)
      throw new IllegalArgumentException("No such plug-in for profile "
          + p.getProfileId());

    List<String> folders = new ArrayList<String>();
    if ("Dropbox".equals(ssd.getTitle())) {
      folders.add("/Ordner1");
      folders.add("/My Dropbox");
    } else {
      folders.add("/Ordner1");
      folders.add("/My Skydrive");
    }

    return folders;
  }

  public void changeProfile(Long profileId, Long jobId, List<String> sourceOptions) {
    Profile p = findProfile(profileId);
    if (p == null)
      throw new IllegalArgumentException("Invalid profile");
    // TODO: Profile must contain options, not the job
  }

  public void uploadDatasourcePlugin(String filename, InputStream data) {
    File f = new File("tmp");
    if (!f.exists())
      f.mkdir();
    System.out.println(f.getAbsolutePath());
    File name = new File(filename);
    try {
      FileOutputStream fos = new FileOutputStream(new File("tmp/"
          + name.getName()));

      byte[] buffer = new byte[5 * 1024 * 1024];
      int readBytes;
      while ((readBytes = data.read(buffer, 0, buffer.length)) >= 0) {
        fos.write(buffer, 0, readBytes);
      }
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void deleteDatasourcePlugin(String name) {
    File plugin = new File("tmp/" + new File(name).getName());
    if (plugin.exists()) {
      plugin.delete();
    }
  }

  // datasink operations

  public List<Profile> getDatasinkProfiles(String username) {
    List<Profile> cloneDs = new ArrayList<Profile>();
    for (Profile p : profiles) {
      if (findSinkDescribable(p.getDescription()) != null
          && p.getUser().getUsername().equals(username)) {
        cloneDs.add(p);
      }
    }
    return cloneDs;
  }

  public List<SourceSinkDescribable> getDatasinks() {
    return sinks;
  }

  public AuthRequest preAuth(String username, String uniqueDescIdentifier,
      String profileName, String keyRing) throws PluginException,
      InvalidCredentialsException {
    SourceSinkDescribable source = findSourceSinkDescribable(uniqueDescIdentifier);
    BackMeUpUser u = findUser(username);

    if (source == null)
      throw new IllegalArgumentException("Unknown plugin "
          + uniqueDescIdentifier);
    if (u == null)
      throw new UnknownUserException(username);

    if (!passwords.get(u).equals(keyRing))
      throw new InvalidCredentialsException();

    Profile p = new Profile(maxId++, u, profileName, source.getId(),
        Type.Source);
    profiles.add(p);

    if ("Dropbox".equals(source.getTitle())) {
      String redirectURL = "https://www.dropbox.com/1/oauth/authorize";
      return new AuthRequest(null, null, redirectURL, p);
    }
    List<RequiredInputField> requiredInputs = new ArrayList<RequiredInputField>();
    requiredInputs.add(new RequiredInputField ("Username", "Username", "The username", true, 0, RequiredInputField.Type.String));
    requiredInputs.add(new RequiredInputField ("Username", "Password", "The password", true, 0, RequiredInputField.Type.Password));
    Map<String, String> typeMapping = new HashMap<String, String>();
    typeMapping.put("Password", "Password");
    typeMapping.put("Username", "String");
    return new AuthRequest(requiredInputs, typeMapping, null, p);
  }

  public void postAuth(Long profileId, Properties props, String keyRing)
      throws PluginException, ValidationException, InvalidCredentialsException {
    Profile p = findProfile(profileId);
    if (p == null)
      throw new IllegalArgumentException("Unknown profile " + profileId);
    if (!passwords.get(p.getUser()).equals(keyRing))
      throw new InvalidCredentialsException();

    for (Object keyObj : props.keySet()) {
      String key = (String) keyObj;
      String value = props.getProperty(key);
      // p.putEntry(key, value);
    }
  }

  public void uploadDatasinkPlugin(String filename, InputStream data) {

  }

  public void deleteDatasinkPlugin(String name) {

  }

  // action operations

  public List<ActionDescribable> getActions() {
    List<ActionDescribable> actions = new ArrayList<ActionDescribable>();
    for (ActionDescribable ac : this.actions.values()) {
      actions.add(ac);
    }
    return actions;
  }

  public List<String> getActionOptions(String actionId) {
    if ("org.backmeup.rsa".equals(actionId)) {
      List<String> results = new ArrayList<String>();
      results.add("512-Bit-Verschluesselung (sonst 256)");
      return results;
    }
    return null;
  }

  public void uploadActionPlugin(String filename, InputStream data) {

  }

  public void deleteActionPlugin(String name) {

  }

  // job operations

  public List<BackupJob> getJobs(String username) {
    List<BackupJob> jobs = new ArrayList<BackupJob>();
    for (BackupJob j : this.jobs) {
      if (j.getUser().getUsername().equals(username))
        jobs.add(j);
    }
    return jobs;
  }

  public ValidationNotes createBackupJob(String username, JobCreationRequest request) {

    BackMeUpUser user = findUser(username);
    if (user == null)
      throw new UnknownUserException(username);

    Set<ProfileOptions> sources = new HashSet<ProfileOptions>();

    for (SourceProfileEntry profile: request.getSourceProfiles()) {
      Profile sourceProfile = findProfile(profile.getId());

      if (sourceProfile == null)
        throw new IllegalArgumentException("Source-profile not found " + profile.getId());      
      ProfileOptions po = new ProfileOptions(sourceProfile, profile.getOptions().values().toArray(new String[]{}));
      sources.add(po);
    }

    Profile sinkProfile = findProfile(request.getSinkProfileId());
    if (sinkProfile == null)
      throw new IllegalArgumentException("Sink-profile not found "
          + request.getSinkProfileId());
    String timeExpression = request.getTimeExpression();
    if (timeExpression == null)
      throw new IllegalArgumentException("Cron expression missing");

    Date start = null;
    long delay = 0;
    if (timeExpression.equalsIgnoreCase("daily")) {
      start = new Date();
      delay = DELAY_DAILY;
    } else if (timeExpression.equalsIgnoreCase("weekly")) {
      start = new Date();
      delay = DELAY_WEEKLY;
    } else if (timeExpression.equalsIgnoreCase("monthly")) {
      start = new Date();
      delay = DELAY_MONTHLY;
    } else {
      start = new Date();
      delay = DELAY_YEARLY;
    }

    BackupJob job = new BackupJob(user, sources, sinkProfile,
        findActions(request.getActions()), start, delay, "TestJob");
    job.setId(maxId++);
    jobs.add(job);
    ValidationNotes vn = new ValidationNotes();
    vn.setJob(job);
    return vn;
  }

  public void deleteJob(String username, Long jobId) {
    BackupJob j = null;
    for (BackupJob job : this.jobs) {
      if (job.getId() == jobId && job.getUser().getUsername().equals(username)) {
        j = job;
        break;
      }
    }
    if (j != null) {
      this.jobs.remove(j);
    }
  }

  public List<Status> getStatus(String username, Long jobId) {
    List<Status> status = new ArrayList<Status>();
    for (Status s : this.status) {
      if (s.getJob().getUser().getUsername().equals(username)) {
            status.add(s);
      }       
    }
    return status;
  }

  public ProtocolDetails getProtocolDetails(String username, String fileId) {
    ProtocolDetails pd = new ProtocolDetails();
    FileInfo fi = new FileInfo("1231L", "Facebook", 13304123,
        "sennenhund.jpg", "image", "http://thumbnails.at?id=1231");
    List<Sink> sinks = new ArrayList<Sink>();
    sinks.add(new Sink("DVD per Post", "13330403", "839482933"));
    sinks.add(new Sink("Dropbox", "13330403",
        "Facebook/2012/Fotos/sennenhund.jpg"));

    List<FileInfo> similar = new ArrayList<FileInfo>();
    similar.add(new FileInfo("1234L", null, 13304123, "sennenhund2.jpg", null,
        "http://thumbnails.at?id=1234"));
    pd.setFileInfo(fi);
    pd.setSinks(sinks);
    pd.setSimilar(similar);
    return pd;
  }

  public ProtocolOverview getProtocolOverview(String username, String duration) {
    ProtocolOverview po = new ProtocolOverview();
    po.setTotalCount("23456345");
    po.setTotalStored("2.2GB");
    Set<Entry> storedAmount = new HashSet<Entry>();
    storedAmount.add(new Entry("Facebook", 30, 400));
    storedAmount.add(new Entry("Twitter", 10, 1500));
    storedAmount.add(new Entry("Moodle", 60, 300));
    po.setStoredAmount(storedAmount);   
    return po;
  }

  // search operations
  public long searchBackup(String username, String keyRingPassword, String query) {
    long id = maxId++;
    List<SearchEntry> responses = new ArrayList<SearchEntry>();
    responses.add(new SearchEntry(Long.toString(maxId++), new Date(), "image", "image1.png",
        "http://athumbnail.png"));
    responses.add(new SearchEntry(Long.toString(maxId++), new Date(), "image", "image2.png",
        "http://athumbnail.png"));
    responses.add(new SearchEntry(Long.toString(maxId++), new Date(), "image", "image3.png",
        "http://athumbnail.png"));
    searches.put(id, new SearchResponse(0, 100, "query", responses));

    List<CountedEntry> bySource = new ArrayList<CountedEntry>();
    bySource.add(new CountedEntry("Facebook", 258));
    bySource.add(new CountedEntry("Flick", 54));

    List<CountedEntry> byType = new ArrayList<CountedEntry>();
    byType.add(new CountedEntry("Fotos", 32));
    byType.add(new CountedEntry("Word", 1));
    searches.put(id, new SearchResponse(0, 100, "query", responses, bySource, byType));
    return id;
  }

  public SearchResponse queryBackup(String username, long searchId,
      String filterType, String filterValue) {
    SearchResponse sr = searches.get(searchId);
    if (sr == null)
      throw new IllegalArgumentException("Unknown searchId " + searchId);

    SearchResponse sr2 = new SearchResponse("query");
    if (filterType == null) {
      sr2.setByType(sr.getByType());
      sr2.setBySource(sr.getBySource());
    } else {
      List<SearchEntry> se = new ArrayList<SearchEntry>();
      for (SearchEntry s : sr.getFiles()) {
        if ("type".equals(filterType) && filterValue.equals(s.getType())) {
          se.add(s);
        } else
          se.add(s);
      }
      sr2.setFiles(se);
    }
    return sr2;
  }
  
  public File getThumbnail(String username, String fileId) {
	  return null;
  }

  public void shutdown() {
  }

  // private misc/helper methods
  private SourceSinkDescribable findSourceSinkDescribable(String uniqueId) {
    SourceSinkDescribable sd = findSourceDescribable(uniqueId);
    if (sd == null)
      sd = findSinkDescribable(uniqueId);
    return sd;
  }

  private SourceSinkDescribable findSourceDescribable(String uniqueId) {
    for (SourceSinkDescribable desc : sources) {
      if (desc.getId().equals(uniqueId))
        return desc;
    }
    return null;
  }

  private SourceSinkDescribable findSinkDescribable(String uniqueId) {
    for (SourceSinkDescribable desc : sinks) {
      if (desc.getId().equals(uniqueId))
        return desc;
    }
    return null;
  }

  private BackMeUpUser findUser(String username) {
    for (BackMeUpUser known : knownUsers) {
      if (known.getUsername().equals(username)) {
        return known;
      }
    }
    return null;
  }

  private Profile findProfile(long profileId) {
    for (Profile p : profiles) {
      if (p.getProfileId() == profileId) {
        return p;
      }
    }
    return null;
  }

  private List<ActionProfile> findActions(List<ActionProfileEntry> actionIds) {
    List<ActionProfile> actionDescs = new ArrayList<ActionProfile>();
    for (ActionProfileEntry action : actionIds) {
      ActionDescribable itm = actions.get(action.getId());
      if (itm != null) {
        actionDescs.add(new ActionProfile(itm.getId(), itm.getPriority()));
      }
    }
    return actionDescs;
  }

  private BackupJob findJob(String username, Long jobId) {
    for (BackupJob job : jobs) {
      if (job.getUser().getUsername().equals(username) && job.getId() == jobId) {
        return job;
      }
    }
    return null;
  }

  @Override
  public Properties getMetadata(String username, Long profileId, String keyRing) {
    BackMeUpUser u = findUser(username);
    if (u == null)
      throw new UnknownUserException(username);
    Profile p = findProfile(profileId);
    SourceSinkDescribable ssd = findSourceSinkDescribable(p.getDescription());
    if (ssd == null)
      throw new IllegalArgumentException("Unknown source/sink with id: "
          + p.getDescription());

    Properties props = new Properties();
    return ssd.getMetadata(props);
  }

  @Override
  public ValidationNotes validateBackupJob(String username, Long jobId,
      String keyRing) {
    BackupJob job = findJob(username, jobId);
    if (job == null) {
      throw new IllegalArgumentException("Unknown job with id: " + jobId);
    }

    ValidationNotes notes = new ValidationNotes();

    // plugin-level validation    
    for (ProfileOptions po : job.getSourceProfiles()) {
      // TODO: start validation of profile
      SourceSinkDescribable ssd = findSourceDescribable(po.getProfile()
          .getDescription());
      if (ssd == null) {
        notes.addValidationEntry(ValidationExceptionType.NoValidatorAvailable, po.getProfile().getDescription());
      }      
    }        
    return notes;
  }

  @Override
  public ValidationNotes validateProfile(String username, Long profileId,
      String keyRing) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addProfileEntries(Long profileId, Properties entries,
      String keyRing) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setUserProperty(String username, String key, String value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteUserProperty(String username, String key) {
    // TODO Auto-generated method stub

  }

  @Override
  public BackMeUpUser verifyEmailAddress(String verificationKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BackMeUpUser requestNewVerificationEmail(String username) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public List<KeyserverLog> getKeysrvLogs (BackMeUpUser user)
  {
	  // TODO Auto-generated method stub
	  return null;
  }

  @Override
  public List<String> getStoredDatasourceOptions(String username,
      Long profileId, Long jobId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void changeActionOptions(String actionId, Long jobId,
      Map<String, String> actionOptions) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ActionProfile getStoredActionOptions(String actionId, Long jobId) {
    // TODO Auto-generated method stub
    return null;
  }
}