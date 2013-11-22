package org.backmeup.logic.impl;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


//TODO: Remove plugin based tests from this class; they should be performed from within python or within the plugin itself!
public class BusinessLogicImplTest {

  //private static BusinessLogic logic;

  @BeforeClass
  public static void setUp() {
	/*
    try {
      /*
       * ApplicationContext context = new ClassPathXmlApplicationContext( new
       * String[] { "spring.xml" }); logic =
       * context.getBean(BusinessLogic.class);
       *
      Weld weld = new Weld();
      WeldContainer container = weld.initialize();
      logic = container.instance().select(BusinessLogic.class).get();

    } catch (Throwable e) {
      do {
        e.printStackTrace();
        e = e.getCause();
      } while (e.getCause() != e && e.getCause() != null);
    }
    */
  }

  @AfterClass
  public static void tearDown() {
    //logic.shutdown();
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
	/*
    try {
      logic.deleteUser("Seppl");
    } catch (Exception e) {
    }
    User u = logic.register("Seppl", "12345678", "12345678", "backmeup1@trash-mail.com");
    logic.verifyEmailAddress(u.getVerificationKey());
    User u2 = logic.getUser("Seppl");

   
    Assert.assertNotNull(u);
    Assert.assertNotNull(u2);
    Assert.assertNotNull(u2.getUserId());
    Assert.assertNotNull(u.getUserId());
    Assert.assertEquals(u.getUserId(), u2.getUserId());   
    Assert.assertEquals(u.getUsername(), u2.getUsername());
    Assert.assertEquals(u.getEmail(), u2.getEmail());
    */
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
	/*
    try {
      logic.deleteUser("Seppl");
    } catch (Exception e) {

    }
    User u = logic.register("Seppl", "superlongpassword", "superlongpassword", "backmeup1@trash-mail.com");
    Assert.assertNotNull(u);
    Assert.assertEquals("Seppl", u.getUsername());    
    Assert.assertEquals("backmeup1@trash-mail.com", u.getEmail());
    
    try {
      u = logic.register("James", "123", "12345678", "backmeup1@trash-mail.com");
      Assert.fail("Should not be reached!");
    } catch (PasswordTooShortException pts) {      
    }
    
    try {
      u = logic.register("James", "12345678", "123", "backmeup1@trash-mail.com");
      Assert.fail("Should not be reached!");
    } catch (PasswordTooShortException pts) {      
    }
    
    try {
      u = logic.register("James", "12345678", "12345678", "invalidmailmail.at");
      Assert.fail("Should not be reached!");
    } catch (NotAnEmailAddressException pts) {      
    }
    
    try {
      u = logic.register("James", "12345678", "12345678", "invalidmailmail.@at");
      Assert.fail("Should not be reached!");
    } catch (NotAnEmailAddressException pts) {      
    }
    
    try {
      u = logic.register("James", "12345678", "12345678", "invalidmailmail@at");
      Assert.fail("Should not be reached!");
    } catch (NotAnEmailAddressException pts) {      
    }
    
    try {
      u = logic.register("James", "12345678", "12345678", "invalidmailmail@.at");
      Assert.fail("Should not be reached!");
    } catch (NotAnEmailAddressException pts) {      
    }
    
    try {
      u = logic.register(null, "12345678", "12345678", "invalidmailmail@.at");
      Assert.fail("Should not be reached!");
    } catch (IllegalArgumentException iae) {      
    }
    
    try {
      u = logic.register("James", null, "12345678", "invalidmailmail@.at");
      Assert.fail("Should not be reached!");
    } catch (IllegalArgumentException iae) {      
    }
    
    try {
      u = logic.register("James", "12345678", null, "invalidmailmail@.at");
      Assert.fail("Should not be reached!");
    } catch (IllegalArgumentException iae) {      
    }
    
    try {
      u = logic.register("James", "12345678", "invalidmailmail@.at", null);
      Assert.fail("Should not be reached!");
    } catch (IllegalArgumentException iae) {    
    }
    */
  }

  @Test
  public void testGetDatasources() {
	/*
    List<SourceSinkDescribable> describables = logic.getDatasources();
    for (SourceSinkDescribable ssd : describables) {
      Assert.assertNotNull(ssd.getTitle());
      Assert.assertNotNull(ssd.getDescription());
      Assert.assertNotNull(ssd.getType());
      Assert.assertNotNull(ssd.getId());
      Assert.assertNotNull(ssd.getImageURL());      
    }
    */
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
      Assert.assertTrue (sources.size() > 0);

      List<SourceSinkDescribable> sinks = logic.getDatasinks();
      Assert.assertTrue (sinks.size() > 0);

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
      Assert.assertNotNull (bj.getId());
      Assert.assertNotNull (bj.getCronExpression());
      Assert.assertNotNull (bj.getUser());
      Assert.assertNotNull (bj.getSinkProfile());
      Assert.assertNotNull (bj.getSourceProfiles());

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
	  /*
		try {
		try {
      		logic.deleteUser("fjungwirth");
    	} catch (Exception e) {
    	}
    	User u = logic.register("fjungwirth", "12345678", "12345678", "jungwirth.florian@gmail.com");
    	logic.verifyEmailAddress(u.getVerificationKey());
    
	} catch (AlreadyRegisteredException are) {
	}
		// register moodle datasource

		AuthRequest ar = logic.preAuth("fjungwirth", "org.backmeup.moodle",
				"My Moodle Profile", "12345678");

		// print ar to shell if needed

		Properties props = new Properties();

		props.setProperty("Username", "backmeup");
		// pw: BMUbmu123!
		
		// local db:
		//props.setProperty("Password", "22598af74c6d2ba1cb00eb639f2e0779");
		// server db:
		props.setProperty("Password", "286bafbb1a9faf4dc4e104a33e222304");
		// server-side bmu moodle plugin has to be installed
		props.setProperty("Moodle Server Url",
				"http://gtn02.gtn-solutions.com/moodle20/");

		logic.postAuth(ar.getProfile().getProfileId(), props, "12345678");
		//logic.validateProfile("fjungwirth", ar.getProfile().getProfileId());

		// register skydrive datasink (changed to dropbox)

		AuthRequest ar2 = logic.preAuth("fjungwirth", "org.backmeup.dropbox",
				"Dropbox-Profile", "12345678");

		// print ar2 to shell if needed

		System.out.println("Open the following URL: " + ar2.getRedirectURL());

		props = new Properties();
		// automatically open web page and get code
		// String data =
		// AuthenticationPerformer.performAuthentication(ar2.getRedirectURL(),
		// new DropboxAutomaticAuthorizer());
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String data = br.readLine();
		
		// data => code=1234567&somethingelse=otherPropery&...
		String[] entries = data.split("&");
		for (String entry : entries) {
			String[] pair = entry.split("=");
			props.setProperty(pair[0], pair[1]);
		}

		logic.postAuth(ar2.getProfile().getProfileId(), props, "12345678");

		List<Long> sources = new ArrayList<Long>();

		sources.add(ar.getProfile().getProfileId());

		// create and exceute a backupjob from moodle to skydrive
		logic.createBackupJob("fjungwirth", sources, ar2.getProfile()
				.getProfileId(), null, null, "now", "12345678");

	*/
  }
  
  @Test
  public void testTwitter() throws IOException {
	/*
	try {
		try {
      		logic.deleteUser("michaela.murauer@yahoo.com");
    	} catch (Exception e) {
    	}
    	User u = logic.register("michaela.murauer@yahoo.com", "12345678", "12345678", "michaela.murauer@yahoo.com");
    	logic.verifyEmailAddress(u.getVerificationKey());
    
	} catch (AlreadyRegisteredException are) {
	}
		
	// register twitter datasource

	AuthRequest ar = logic.preAuth("michaela.murauer@yahoo.com", "org.backmeup.twitter",
		"My Twitter Profile", "12345678");

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
	logic.postAuth(ar.getProfile().getProfileId(), props, "12345678");
		
	//logic.validateProfile("mmurauer", ar.getProfile().getProfileId());

	// register skydrive datasink (changed to dropbox)

	AuthRequest ar2 = logic.preAuth("michaela.murauer@yahoo.com", "org.backmeup.dropbox",
			"Dropbox-Profile", "12345678");

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

	logic.postAuth(ar2.getProfile().getProfileId(), props, "12345678");

	List<Long> sources = new ArrayList<Long>();

	sources.add(ar.getProfile().getProfileId());

	// create and exceute a backupjob from moodle to skydrive
	logic.createBackupJob("michaela.murauer@yahoo.com", sources, ar2.getProfile()
			.getProfileId(), null, null, "now", "12345678");
	*/
	}
  @Test
  public void testFacebook() throws IOException {
	  /*try {
			try {
	      		logic.deleteUser("michaela.murauer@yahoo.com");
	    	} catch (Exception e) {
	    	}
	    	User u = logic.register("michaela.murauer@yahoo.com", "12345678", "12345678", "michaela.murauer@yahoo.com");
	    	logic.verifyEmailAddress(u.getVerificationKey());
	    
		} catch (AlreadyRegisteredException are) {
		}	
	// register twitter datasource

	AuthRequest ar = logic.preAuth("michaela.murauer@yahoo.com", "org.backmeup.facebook",
		"My Facebook Profile", "12345678");

	Properties props = new Properties();

		
	System.out.println("Open the following URL: " + ar.getRedirectURL());
		
	props = new Properties();
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String input = br.readLine();
	
	String data = input.substring(input.indexOf('#')+1);
	String[] entries = data.split("&");
	for (String entry : entries) {
		String[] pair = entry.split("=");
		props.setProperty(pair[0], pair[1]);
	}
	
	logic.postAuth(ar.getProfile().getProfileId(), props, "12345678");
		
	//logic.validateProfile("mmurauer", ar.getProfile().getProfileId());

	// register skydrive datasink (changed to dropbox)

	AuthRequest ar2 = logic.preAuth("michaela.murauer@yahoo.com", "org.backmeup.dropbox",
			"Dropbox-Profile", "12345678");

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

	logic.postAuth(ar2.getProfile().getProfileId(), props, "12345678");

	List<Long> sources = new ArrayList<Long>();

	sources.add(ar.getProfile().getProfileId());

	// create and exceute a backupjob from moodle to skydrive
	logic.createBackupJob("michaela.murauer@yahoo.com", sources, ar2.getProfile()
			.getProfileId(), null, null, "now", "12345678");
	*/
	}
}
