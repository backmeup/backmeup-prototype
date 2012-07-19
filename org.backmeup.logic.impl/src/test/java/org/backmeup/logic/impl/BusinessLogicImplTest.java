package org.backmeup.logic.impl;

import java.io.IOException;
import java.util.List;

import org.backmeup.logic.BusinessLogic;
import org.backmeup.model.User;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

//TODO: Remove plugin based tests from this class; they should be performed from within python or within the plugin itself!
public class BusinessLogicImplTest {

  private static BusinessLogic logic;

  @BeforeClass
  public static void setUp() {
    try {
      /*
       * ApplicationContext context = new ClassPathXmlApplicationContext( new
       * String[] { "spring.xml" }); logic =
       * context.getBean(BusinessLogic.class);
       */
      Weld weld = new Weld();
      WeldContainer container = weld.initialize();
      logic = container.instance().select(BusinessLogic.class).get();

    } catch (Throwable e) {
      do {
        e.printStackTrace();
        e = e.getCause();
      } while (e.getCause() != e && e.getCause() != null);
    }
  }

  @AfterClass
  public static void tearDown() {
    logic.shutdown();
  }

  /*
   * @Test public void testSomething() throws IOException { // your plugins
   * describable returns e.g.: org.backmeup.moodle try {
   * logic.register("fjungwirth", "apassword", "apassword",
   * "fjungwirth@something.at"); } catch (AlreadyRegisteredException are) {
   * 
   * } // register moodle datasource AuthRequest ar =
   * logic.preAuth("fjungwirth", "org.backmeup.dropbox",
   * "My Dropbox-Source Profile", true, "apassword");
   * 
   * System.out.println("Open the following URL: " + ar.getRedirectURL());
   * BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
   * // print ar to shell if needed Properties props = new Properties();
   * System.out.print("code: "); props.setProperty("code", br.readLine());
   * logic.postAuth(ar.getProfile().getProfileId(), props, "apassword");
   * 
   * // register skydrive datasink AuthRequest ar2 = logic.preAuth("fjungwirth",
   * "org.backmeup.skydrive", "My Skydrive-Sink Profile", false, "apassword");
   * // print ar2 to shell if needed
   * System.out.println("Open the following URL: " + ar2.getRedirectURL());
   * props = new Properties(); System.out.print("code: "); String code =
   * br.readLine(); props.setProperty("code", code);
   * logic.postAuth(ar2.getProfile().getProfileId(), props, "apassword");
   * 
   * List<Long> sources = new ArrayList<Long>();
   * sources.add(ar.getProfile().getProfileId()); // create and exceute a
   * backupjob from moodle to skydrive logic.createBackupJob("fjungwirth",
   * sources, ar2.getProfile().getProfileId(), null, null, "now", "apassword");
   * 
   * }
   */

  @Test
  public void testGetProfileDao() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetUserDao() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetUser() {
    try {
      logic.deleteUser("Seppl");
    } catch (Exception e) {
    }
    User u = logic.register("Seppl", "apass", "ringring", "amail.at");
    User u2 = logic.getUser("Seppl");
    assert (u != null);
    assert (u2 != null);
    assert (u2.getUserId() != null);
    assert (u.getUserId() != null);
    assert (u.getUserId().equals(u2.getUserId()));
    assert (u.getPassword().equals(u2.getPassword()));
    assert (u.getUsername().equals(u2.getUsername()));
    assert (u.getEmail().equals(u2.getEmail()));
    assert (u.getKeyRing().equals(u2.getKeyRing()));
  }

  @Test
  public void testDeleteUser() {
    // fail("Not yet implemented");
  }

  @Test
  public void testChangeUser() {
    // fail("Not yet implemented");
  }

  @Test
  public void testLogin() {
    // fail("Not yet implemented");
  }

  @Test
  public void testRegister() {
    try {
      logic.deleteUser("Seppl");
    } catch (Exception e) {

    }
    User u = logic.register("Seppl", "apass", "ringring", "amail.at");
    assert (u != null);
    assert ("Seppl".equals(u.getUsername()));
    assert ("apass".equals(u.getPassword()));
    assert ("ringring".equals(u.getKeyRing()));
    assert ("amail.at".equals(u.getEmail()));
  }

  @Test
  public void testGetDatasources() {
    List<SourceSinkDescribable> describables = logic.getDatasources();
    for (SourceSinkDescribable ssd : describables) {
      assert (ssd.getTitle() != null);
      assert (ssd.getDescription() != null);
      assert (ssd.getType() != null);
      assert (ssd.getId() != null);
      assert (ssd.getImageURL() != null);
    }
  }

  @Test
  public void testGetDatasourceProfiles() {
    // fail("Not yet implemented");
  }

  @Test
  public void testDeleteProfile() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetDatasourceOptions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testChangeProfile() {
    // fail("Not yet implemented");
  }

  @Test
  public void testUploadDatasourcePlugin() {
    // fail("Not yet implemented");
  }

  @Test
  public void testDeleteDatasourcePlugin() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetDatasinks() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetDatasinkProfiles() {
    // fail("Not yet implemented");
  }

  @Test
  public void testUploadDatasinkPlugin() {
    // fail("Not yet implemented");
  }

  @Test
  public void testDeleteDatasinkPlugin() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetActions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetActionOptions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testUploadActionPlugin() {
    // fail("Not yet implemented");
  }

  @Test
  public void testDeleteActionPlugin() {
    // fail("Not yet implemented");
  }

  @Test
  public void testCreateBackupJob() {
    /*
    try {
      User u;
      try {
        u = logic.register("backuper", "hi", "hi", "amail");
      } catch (AlreadyRegisteredException are) {
        u = logic.getUser("backuper");
      }

      List<SourceSinkDescribable> sources = logic.getDatasources();
      assert (sources.size() > 0);

      List<SourceSinkDescribable> sinks = logic.getDatasinks();
      assert (sinks.size() > 0);

      System.out.println("Register source:");
      AuthRequest ar = logic.preAuth(u.getUsername(), "org.backmeup.dropbox",
          "Dropbox-Profile", "hi");
      String url = ar.getRedirectURL();
      System.out.println(url);
      String data = AuthenticationPerformer.performAuthentication(url,
          new DropboxAutomaticAuthorizer());
      Properties p = new Properties();
      String[] entries = data.split("&");
      for (String entry : entries) {
        String[] pair = entry.split("=");
        p.setProperty(pair[0], pair[1]);
      }
      logic.postAuth(ar.getProfile().getProfileId(), p, "hi");

      System.out.println("Using source as sink...");
      Long sinkProfileId = ar.getProfile().getProfileId();
      List<Long> sourcesList = new ArrayList<Long>();
      sourcesList.add(ar.getProfile().getProfileId());
      BackupJob bj = logic.createBackupJob(u.getUsername(), sourcesList,
          sinkProfileId, null, null, "weekly", "hi");
      assert (bj.getId() != null);
      assert (bj.getCronExpression() != null);
      assert (bj.getUser() != null);
      assert (bj.getSinkProfile() != null);
      assert (bj.getSourceProfiles() != null);

      List<Status> results = logic
          .getStatus("backuper", bj.getId(), null, null);
      for (Status s : results) {
        System.out.println(s.getProgress() + ", " + s.getType() + ": "
            + s.getMessage());
      }

    } catch (Throwable t) {
      t.printStackTrace();
      while (t != t.getCause()) {
        t = t.getCause();
        if (t != null)
          t.printStackTrace();
      }
    }*/
  }

  @Test
  public void testGetJobs() {
    // fail("Not yet implemented");
  }

  @Test
  public void testDeleteJob() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetStatus() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetProtocolDetails() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetProtocolOverview() {
    // fail("Not yet implemented");
  }

  @Test
  public void testPreAuth() {
    // fail("Not yet implemented");
  }

  @Test
  public void testPostAuth() {
    // fail("Not yet implemented");
  }

  @Test
  public void testSearchBackup() {
    // fail("Not yet implemented");
  }

  @Test
  public void testQueryBackup() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetDataAccessLayer() {
    // fail("Not yet implemented");
  }

  @Test
  public void testSetDataAccessLayer() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetPlugins() {
    // fail("Not yet implemented");
  }

  @Test
  public void testSetPlugins() {
    // fail("Not yet implemented");
  }

  @Test
  public void testMoodle() throws IOException {
    /*try {     
      User u = logic.register("fjungwirth", "123", "123",
          "jungwirth.florian@gmail.com");

    } catch (AlreadyRegisteredException are) {
    }

    // register moodle datasource

    AuthRequest ar = logic.preAuth("fjungwirth", "org.backmeup.moodle",
        "My Moodle Profile", "123");

    // print ar to shell if needed

    Properties props = new Properties();

    props.setProperty("Username", "backmeup");
    // pw: BMUbmu123!
    props.setProperty("Password", "286bafbb1a9faf4dc4e104a33e222304");
    // server-side bmu moodle plugin has to be installed
    props.setProperty("Moodle Server Url",
        "http://gtn02.gtn-solutions.com/moodle20");

    logic.postAuth(ar.getProfile().getProfileId(), props, "123");

    // register skydrive datasink (changed to dropbox)

    AuthRequest ar2 = logic.preAuth("fjungwirth", "org.backmeup.dropbox",
        "Dropbox-Profile", "123");

    // print ar2 to shell if needed

    System.out.println("Open the following URL: " + ar2.getRedirectURL());

    props = new Properties();

    // automatically open web page and get code
    String data = AuthenticationPerformer.performAuthentication(
        ar2.getRedirectURL(), new DropboxAutomaticAuthorizer());
    // data => code=1234567&somethingelse=otherPropery&...
    String[] entries = data.split("&");
    for (String entry : entries) {
      String[] pair = entry.split("=");
      props.setProperty(pair[0], pair[1]);
    }

    logic.postAuth(ar2.getProfile().getProfileId(), props, "123");

    List<Long> sources = new ArrayList<Long>();

    sources.add(ar.getProfile().getProfileId());

    // create and exceute a backupjob from moodle to skydrive

    logic.createBackupJob("fjungwirth", sources, ar2.getProfile()
        .getProfileId(), null, null, "now", "123");
     */
  }
  
  @Test
  public void testTwitter() throws IOException {
	/*try {
		User u = logic.register("mmurauer", "123", "123",
				"michaela.murauer@yahoo.com");
	} catch (AlreadyRegisteredException are) {
	}
		
	// register twitter datasource

	AuthRequest ar = logic.preAuth("mmurauer", "org.backmeup.twitter",
		"My Twitter Profile", "123");

	Properties props = new Properties();

		
	System.out.println("Open the following URL: " + ar.getRedirectURL());
		
	props = new Properties();
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String data = br.readLine();
	String[] tmp = data.split("\\?");
		
	// data => code=1234567&somethingelse=otherPropery&...
	String[] entries = tmp[1].split("&");
	for (String entry : entries) {
		String[] pair = entry.split("=");
		props.setProperty(pair[0], pair[1]);
	}
	logic.postAuth(ar.getProfile().getProfileId(), props, "123");
		
	logic.validateProfile("mmurauer", ar.getProfile().getProfileId());

	// register skydrive datasink (changed to dropbox)

	AuthRequest ar2 = logic.preAuth("mmurauer", "org.backmeup.dropbox",
			"Dropbox-Profile", "123");

	System.out.println("Open the following URL: " + ar2.getRedirectURL());

	props = new Properties();
	br = new BufferedReader(new InputStreamReader(System.in));
	data = br.readLine();
		
	// data => code=1234567&somethingelse=otherPropery&...
	entries = data.split("&");
	for (String entry : entries) {
		String[] pair = entry.split("=");
		props.setProperty(pair[0], pair[1]);
	}

	logic.postAuth(ar2.getProfile().getProfileId(), props, "123");

	List<Long> sources = new ArrayList<Long>();

	sources.add(ar.getProfile().getProfileId());

	// create and exceute a backupjob from moodle to skydrive
	logic.createBackupJob("mmurauer", sources, ar2.getProfile()
			.getProfileId(), null, null, "now", "123");
	*/
	}
}
