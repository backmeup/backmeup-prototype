package org.backmeup.keyserver.client.test;

import org.junit.BeforeClass;

public class KeyserverTest {
  //private static Keyserver ks;

  @BeforeClass
  public static void setUp() {
    try {
      /*
       * ApplicationContext context = new ClassPathXmlApplicationContext( new
       * String[] { "spring.xml" }); logic =
       * context.getBean(BusinessLogic.class);
       */
      //Weld weld = new Weld();
      //WeldContainer container = weld.initialize();
     // ks = container.instance().select(Keyserver.class).get();

    } catch (Throwable e) {
      do {
        e.printStackTrace();
        e = e.getCause();
      } while (e.getCause() != e && e.getCause() != null);
    }
  }
  /*
  @Test
  public void testRegisterUser() {
    if (!ks.isUserRegistered(200L))
      ks.registerUser(200L, "apassword");
    
    Assert.assertTrue(ks.validateUser(200L, "apassword"));    
  }
  
  @Test
  public void testRegisterService() {   
    if (!ks.isServiceRegistered(201L))
      ks.addService(201L);
    Assert.assertTrue(ks.isServiceRegistered(201L));
    if (!ks.isServiceRegistered(202L))
      ks.addService(202L);
    Assert.assertTrue(ks.isServiceRegistered(202L));
  }
  
  @Test
  public void testAddAuthInfo() {
    
    Properties p = new Properties();
    p.setProperty("oauthtoken", "asdfasdf");
    p.setProperty("oauthpassword", "asdfasdf2");
    
    Properties p2 = new Properties();
    p2.setProperty("mailaddress", "something@somewhere.at");
    p2.setProperty("kind", "smtp");
    
    Properties p3 = new Properties();
    p3.setProperty("moredata", "as-dfadjsfasldf#++*\"");
    p3.setProperty("yep", "tests");
    
    if (ks.isAuthInformationAvailable(202L, 200L, 201L, "apassword")) {
      ks.deleteAuthInfo(202L);
    }
        
    if (ks.isAuthInformationAvailable(203L, 200L, 202L, "apassword")) {
      ks.deleteAuthInfo(203L);
    }
       
    if (ks.isAuthInformationAvailable(204L, 200L, 201L, "apassword")) {
      ks.deleteAuthInfo(204L);
    }
    
    if (!ks.isAuthInformationAvailable(202L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 202L, p);
    
    if (!ks.isAuthInformationAvailable(203L, 200L, 202L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 202L, 203L, p2);
       
    if (!ks.isAuthInformationAvailable(204L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 204L, p3);
    
    Assert.assertTrue(ks.isAuthInformationAvailable(204L, 200L, 201L, "apassword"));
    Assert.assertTrue(ks.isAuthInformationAvailable(203L, 200L, 202L, "apassword"));
    Assert.assertTrue(ks.isAuthInformationAvailable(202L, 200L, 201L, "apassword"));
    
  }
    
  @Test
  public void testGetToken() {
    // add authentication data first
    if (!ks.isUserRegistered(200L))
      ks.registerUser(200L, "apassword");
    
    if (!ks.isServiceRegistered(201L))
      ks.addService(201L);
    if (!ks.isServiceRegistered(202L))
      ks.addService(202L);
    
    Properties p = new Properties();
    p.setProperty("oauthtoken", "asdfasdf");
    p.setProperty("oauthpassword", "asdfasdf2");
    
    Properties p2 = new Properties();
    p2.setProperty("mailaddress", "something@somewhere.at");
    p2.setProperty("kind", "smtp");
    
    Properties p3 = new Properties();
    p3.setProperty("moredata", "as-dfadjsfasldf#++*\"");
    p3.setProperty("yep", "tests");
    
    if (!ks.isAuthInformationAvailable(202L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 202L, p);
    
    if (!ks.isAuthInformationAvailable(203L, 200L, 202L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 202L, 203L, p2);
       
    if (!ks.isAuthInformationAvailable(204L, 200L, 201L, "apassword"))
      ks.addAuthInfo(200L, "apassword", 201L, 204L, p3);
    
    // get a token and test 
    Token t = ks.getToken(200L, "apassword", new Long[]{201L}, new Long[]{202L}, new Date().getTime(), true, "blub");
    System.out.println("Token: " + t.getToken() + " / " + t.getTokenId());
    Assert.assertNotNull(t.getToken());
    Assert.assertNotNull(t.getTokenId());
    // data might be crawled after 3 seconds again
    t.setBackupdate(new Date().getTime() + 3000);
    AuthDataResult authData = ks.getData(t);
    Assert.assertNotNull(authData.getNewToken());
    t = authData.getNewToken();
    for (AuthData ad  : authData.getAuthinfos()) {
      Assert.assertTrue(ad.getAi_data().containsKey("oauthtoken"));
      Assert.assertEquals("asdfasdf", ad.getAi_data().get("oauthtoken"));
      Assert.assertTrue(ad.getAi_data().containsKey("oauthpassword"));
      Assert.assertEquals("asdfasdf2", ad.getAi_data().get("oauthpassword"));      
    }
    
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    t.setBackupdate(new Date().getTime() + 3000);
    authData = ks.getData(t);
    for (AuthData ad  : authData.getAuthinfos()) {
      Assert.assertTrue(ad.getAi_data().containsKey("oauthtoken"));
      Assert.assertEquals("asdfasdf", ad.getAi_data().get("oauthtoken"));
      Assert.assertTrue(ad.getAi_data().containsKey("oauthpassword"));
      Assert.assertEquals("asdfasdf2", ad.getAi_data().get("oauthpassword"));      
    }
    
    t = ks.getToken(200L, "apassword", new Long[]{201L, 202L}, new Long[]{202L, 204L}, new Date().getTime(), true, "blub");
    System.out.println("Token: " + t.getToken() + " / " + t.getTokenId());
    authData = ks.getData(t);
    for (AuthData ad  : authData.getAuthinfos()) {
      if (ad.getAi_data().containsKey("oauthtoken")) {
        Assert.assertTrue(ad.getAi_data().containsKey("oauthtoken"));
        Assert.assertEquals("asdfasdf", ad.getAi_data().get("oauthtoken"));
        Assert.assertTrue(ad.getAi_data().containsKey("oauthpassword"));
        Assert.assertEquals("asdfasdf2", ad.getAi_data().get("oauthpassword"));
      } else {
        Assert.assertTrue(ad.getAi_data().containsKey("moredata"));
        Assert.assertEquals("as-dfadjsfasldf#++*\"", ad.getAi_data().get("moredata"));
        Assert.assertTrue(ad.getAi_data().containsKey("yep"));
        Assert.assertEquals("tests", ad.getAi_data().get("yep"));
      }
    }
    
    if (ks.isUserRegistered(200L))
      ks.deleteUser(200L);
  }*/
}
